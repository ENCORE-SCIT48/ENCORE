package com.encore.encore.domain.chat.service;

import com.encore.encore.domain.chat.dto.RequestChatMessage;
import com.encore.encore.domain.chat.dto.ResponseChatExitDto;
import com.encore.encore.domain.chat.dto.ResponseChatMessage;
import com.encore.encore.domain.chat.dto.ResponseParticipantDto;
import com.encore.encore.domain.chat.entity.ChatMessage;
import com.encore.encore.domain.chat.entity.ChatParticipant;
import com.encore.encore.domain.chat.entity.ChatRoom;
import com.encore.encore.domain.chat.repository.ChatMessageRepository;
import com.encore.encore.domain.chat.repository.ChatParticipantRepository;
import com.encore.encore.domain.chat.repository.ChatPostRepository;
import com.encore.encore.domain.chat.repository.ChatRoomRepository;
import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.member.service.ProfileService;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatPostRepository chatPostRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ProfileService profileService;

    /**
     * 채팅방에 메시지를 전송합니다.
     *
     * <p>주어진 채팅방(Room)에 현재 프로필(사용자/공연자/주최자)의 메시지를 저장하고,
     * 저장된 메시지를 {@link ResponseChatMessage} DTO로 반환합니다.</p>
     *
     * @param roomId     메시지를 전송할 채팅방의 ID
     * @param activeId   현재 로그인한 사용자의 프로필 ID
     * @param activeMode 현재 로그인한 사용자의 활동 모드(관람객, 공연자, 주최자)
     * @param request    전송할 메시지 내용이 담긴 {@link RequestChatMessage} 객체
     * @return 저장된 메시지를 기반으로 생성된 {@link ResponseChatMessage} 객체
     * @throws IllegalArgumentException 채팅방(roomId)에 해당하는 채팅방이 존재하지 않을 경우
     */
    public ResponseChatMessage sendMessage(Long roomId, Long activeId, ActiveMode activeMode, RequestChatMessage request) {
        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "채팅방 없음"));

        ChatMessage message = ChatMessage.builder()
            .room(room)
            .profileId(activeId)
            .profileMode(activeMode)
            .content(request.getContent())
            .sentAt(LocalDateTime.now())
            .build();

        chatMessageRepository.save(message);

        return ResponseChatMessage.builder()
            .messageId(message.getMessageId())
            .profileId(message.getProfileId())
            .senderName(profileService.resolveSenderName(message.getMessageId(), message.getProfileMode()))
            .content(message.getContent())
            .createdAt(message.getCreatedAt())
            .build();

    }

    /**
     * 특정 채팅방의 모든 메시지를 조회합니다.
     *
     * <p>조회된 메시지들은 생성일(createdAt) 기준으로 오름차순 정렬되며,
     * {@link ResponseChatMessage} DTO 리스트로 반환됩니다.</p>
     *
     * @param roomId 조회할 채팅방의 ID
     * @return 채팅방에 속한 메시지들을 {@link ResponseChatMessage} 형태로 변환한 리스트
     */
    public List<ResponseChatMessage> getMessages(Long roomId) {

        return chatMessageRepository.findByRoomRoomIdOrderByCreatedAtAsc(roomId)
            .stream()
            .map(message -> ResponseChatMessage.builder()
                .messageId(message.getMessageId())
                .profileId(message.getProfileId())
                .senderName(profileService.resolveSenderName(message.getProfileId(), message.getProfileMode()))
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build()
            )
            .toList();
    }


    /**
     * 채팅방 퇴장 처리
     *
     * @param roomId
     * @param activeProfileId
     * @param activeMode
     */
    public ResponseChatExitDto leaveChat(Long roomId, Long activeProfileId, ActiveMode activeMode) {
        log.info("[Service] 채팅방 퇴장 처리 시작 - roomId: {}, activeProfileId: {}, activeMode: {}", roomId, activeProfileId, activeMode);

        // 1. 참여 정보 확인
        ChatParticipant chatParticipant = chatParticipantRepository
            .findByRoom_RoomIdAndProfileIdAndProfileModeAndIsDeletedFalse(roomId, activeProfileId, activeMode)
            .orElse(null);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "채팅방 없음"));

        Long writerId = chatRoom.getChatPost().getProfileId();
        String writerMode = chatRoom.getChatPost().getProfileMode().name();

        // 2. 방장(글쓴이) 체크
        if (activeProfileId.equals(writerId) && activeMode.name().equals(writerMode)) {
            return ResponseChatExitDto.builder()
                .roomId(roomId)
                .exitSuccess(false)
                .isOwner(true)
                .message("글쓴이는 퇴장할 수 없습니다.")
                .build();
        }

        // 3. 참여 정보 Soft Delete
        if (chatParticipant != null) {
            chatParticipant.delete();
            chatParticipantRepository.save(chatParticipant);
            log.info("[Service] 멤버 상태 변경 완료 - is_deleted: 1");

            // 4. 채팅방 인원수 감소
            chatRoom.getChatPost().setCurrentMember(chatRoom.getChatPost().getCurrentMember() - 1);

            log.info("[Service] 채팅방 인원수 감소 완료 - 현재 인원: {}", chatRoom.getChatPost().getCurrentMember());
        } else {
            return ResponseChatExitDto.builder()
                .roomId(roomId)
                .exitSuccess(false)
                .isOwner(false)
                .message("참여자가 아니거나 이미 나간 상태입니다.")
                .build();
        }

        // 5. 정상 퇴장 반환
        return ResponseChatExitDto.builder()
            .roomId(roomId)
            .exitSuccess(true)
            .isOwner(false)
            .message("채팅방을 나갔습니다.")
            .build();
    }

    /**
     * 채팅방 참여자 목록을 조회
     *
     * @param roomId
     * @return
     */
    public List<ResponseParticipantDto> getParticipantList(Long roomId) {

        List<ChatParticipant> participantList = chatParticipantRepository.findByRoomRoomIdAndIsDeletedFalse(roomId);


        List<ResponseParticipantDto> responseParticipantDtoList = new ArrayList<>();

        for (ChatParticipant chatParticipant : participantList) {
            ResponseParticipantDto dto = ResponseParticipantDto.builder()
                .participantId(chatParticipant.getParticipantId())
                .nickName(profileService.resolveSenderName(
                    chatParticipant.getProfileId(),
                    chatParticipant.getProfileMode()))
                .activeId(chatParticipant.getParticipantId())
                .activeMode(chatParticipant.getProfileMode().name())
                .build();
            responseParticipantDtoList.add(dto);
        }
        return responseParticipantDtoList;
    }
}

