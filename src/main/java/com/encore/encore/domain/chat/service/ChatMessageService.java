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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
     * [설명] 지정된 채팅방(roomId)에 메시지를 전송합니다.
     * <p>
     * 현재 로그인한 사용자의 프로필 ID와 활동 모드를 기반으로 메시지를 저장하고,
     * 저장된 메시지를 {@link ResponseChatMessage} 형태로 반환합니다.
     * </p>
     *
     * @param roomId     메시지를 전송할 채팅방의 ID
     * @param activeId   현재 로그인한 사용자의 프로필 ID
     * @param activeMode 현재 로그인한 사용자의 활동 모드({@link ActiveMode})
     * @param request    전송할 메시지 내용이 담긴 {@link RequestChatMessage} DTO
     * @return 저장된 메시지를 기반으로 생성된 {@link ResponseChatMessage} 객체
     * @throws ApiException 채팅방(roomId)에 해당하는 채팅방이 존재하지 않을 경우 {@link ErrorCode#NOT_FOUND}
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
            .profileMode(message.getProfileMode().name())
            .senderName(profileService.resolveSenderName(message.getProfileId(), message.getProfileMode()))
            .content(message.getContent())
            .createdAt(message.getCreatedAt())
            .isMine(message.getProfileId().equals(activeId) && message.getProfileMode().equals(activeMode))
            .build();

    }

    /**
     * [설명] 특정 채팅방(roomId)의 메시지 목록을 조회합니다.
     * <p>
     * 조회된 메시지는 생성일(createdAt) 기준 오름차순 정렬되며,
     * {@link ResponseChatMessage} 리스트로 반환됩니다.
     * 페이지네이션(page, size)을 지원합니다.
     * </p>
     *
     * @param roomId 조회할 채팅방 ID
     * @param page   조회할 페이지 번호 (0부터 시작)
     * @param size   한 페이지당 메시지 수
     * @return {@link ResponseChatMessage} DTO로 변환된 메시지 리스트
     */
    public List<ResponseChatMessage> getMessages(Long roomId, int page, int size, Long activeId, ActiveMode activeMode) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());

        return chatMessageRepository.findByRoomRoomId(roomId, pageable)
            .stream()
            .map(message -> ResponseChatMessage.builder()
                .messageId(message.getMessageId())
                .profileId(message.getProfileId())
                .profileMode(message.getProfileMode().name())
                .senderName(profileService.resolveSenderName(message.getProfileId(), message.getProfileMode()))
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .isMine(message.getProfileId().equals(activeId) && message.getProfileMode().equals(activeMode))
                .build()
            )
            .toList();
    }


    /**
     * [설명] 지정된 채팅방(roomId)에서 사용자를 퇴장 처리합니다.
     * <p>
     * 참여 여부를 확인하고, 참여자인 경우 Soft Delete 처리 후 채팅방 인원수를 감소시킵니다.
     * 글쓴이(방장)인 경우 퇴장 불가로 처리합니다.
     * </p>
     *
     * @param roomId          퇴장할 채팅방 ID
     * @param activeProfileId 퇴장 요청한 사용자의 프로필 ID
     * @param activeMode      퇴장 요청한 사용자의 활동 모드({@link ActiveMode})
     * @return 퇴장 처리 결과를 담은 {@link ResponseChatExitDto} 객체
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
     * [설명] 채팅방(roomId)의 참여자 목록을 조회합니다.
     * <p>
     * 참여자의 프로필 ID, 닉네임, 참여 모드를 {@link ResponseParticipantDto}로 매핑하여 반환합니다.
     * </p>
     *
     * @param roomId 조회할 채팅방 ID
     * @return {@link ResponseParticipantDto} 리스트
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
                .profileId(chatParticipant.getProfileId())
                .profileMode(chatParticipant.getProfileMode().name())
                .build();
            responseParticipantDtoList.add(dto);
        }
        return responseParticipantDtoList;
    }
}

