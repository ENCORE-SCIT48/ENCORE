package com.encore.encore.domain.chat.service;

import com.encore.encore.domain.chat.dto.ResponseChatMessage;
import com.encore.encore.domain.chat.dto.dm.RequestDmDto;
import com.encore.encore.domain.chat.dto.dm.RequestSendDmDto;
import com.encore.encore.domain.chat.dto.dm.ResponseDmRoomStatusDto;
import com.encore.encore.domain.chat.dto.dm.ResponseListDmDto;
import com.encore.encore.domain.chat.entity.ChatMessage;
import com.encore.encore.domain.chat.entity.ChatParticipant;
import com.encore.encore.domain.chat.entity.ChatRoom;
import com.encore.encore.domain.chat.repository.ChatMessageRepository;
import com.encore.encore.domain.chat.repository.ChatParticipantRepository;
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
import java.util.Optional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class DmService {

    private final ProfileService profileService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * 로그인한 사용자의 Pending 상태인 DM 참여 내역을 조회합니다.
     *
     * <p>메서드 동작:
     * <ul>
     *     <li>로그인한 사용자의 프로필 ID와 프로필 모드에 해당하는 Pending DM 참s여 목록을 조회합니다.</li>
     *     <li>각 참여 채팅방에서 상대방 참가자를 조회합니다 (본인 제외).</li>
     *     <li>상대방의 닉네임은 ProfileService를 통해 조회합니다.</li>
     *     <li>각 채팅방에서 최신 메시지를 조회하여 DTO에 포함합니다.</li>
     * </ul>
     *
     * @param activeProfileId 로그인한 사용자의 프로필 ID
     * @param activeMode      로그인한 사용자의 프로필 모드 (USER, PERFORMER, HOST)
     * @return 로그인 사용자가 참여 중인 Pending DM 목록을 담은 {@link ResponseListDmDto} 리스트
     * @throws RuntimeException 상대방 참가자가 없는 경우 발생
     */
    public List<ResponseListDmDto> getPendingList(Long activeProfileId, ActiveMode activeMode) {
        List<ChatParticipant> myPendingList = chatParticipantRepository
            .findByProfileIdAndProfileModeAndParticipantStatusAndRoom_RoomType(
                activeProfileId,
                activeMode,
                ChatParticipant.ParticipantStatus.PENDING,
                ChatRoom.RoomType.DM
            );

        List<ResponseListDmDto> dtoList = new ArrayList<>();

        for (ChatParticipant myCp : myPendingList) {
            ChatParticipant other = chatParticipantRepository.findOtherParticipantInRoom(
                myCp.getRoom().getRoomId(),
                activeProfileId,
                activeMode
            ).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "상대방 참가자가 없습니다."));

            String nickname = profileService.resolveSenderName(other.getProfileId(), other.getProfileMode());

            ChatMessage latest = chatMessageRepository
                .findTopByRoom_RoomIdOrderBySentAtDesc(myCp.getRoom().getRoomId())
                .orElse(null);

            dtoList.add(ResponseListDmDto.builder()
                .roomId(myCp.getRoom().getRoomId())
                .otherProfileId(other.getProfileId())
                .otherUserNickname(nickname)
                .otherProfileMode(other.getProfileMode().name())
                .latestMessage(latest != null ? latest.getContent() : null)
                .latestMessageAt(latest != null ? latest.getSentAt() : null)
                .status(myCp.getParticipantStatus().name())
                .build());
        }

        return dtoList;
    }


    /**
     * 로그인한 사용자의 참여 중(ACCEPTED) DM 목록을 조회합니다.
     *
     * @param activeProfileId 로그인한 사용자의 프로필 ID
     * @param activeMode      로그인한 사용자의 ActiveMode (USER / PERFORMER / HOST)
     * @return 참여 중인 DM 정보를 담은 ResponseListDmDto 리스트
     */
    public List<ResponseListDmDto> getAcceptedList(Long activeProfileId, ActiveMode activeMode) {

        // 나의 ACCEPTED 상태 DM 참여 내역 조회
        List<ChatParticipant> myAcceptedList = chatParticipantRepository
            .findByProfileIdAndProfileModeAndParticipantStatusAndRoom_RoomType(
                activeProfileId,
                activeMode,
                ChatParticipant.ParticipantStatus.ACCEPTED,  // pending -> accepted
                ChatRoom.RoomType.DM
            );

        List<ResponseListDmDto> dtoList = new ArrayList<>();

        for (ChatParticipant myCp : myAcceptedList) {

            // 상대방 조회: 나와 profileId, profileMode 모두 다른 사람
            ChatParticipant other = chatParticipantRepository.findOtherParticipantInRoom(
                myCp.getRoom().getRoomId(),
                activeProfileId,
                activeMode
            ).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "상대방 참가자가 없습니다."));

            String nickname = profileService.resolveSenderName(other.getProfileId(), other.getProfileMode());

            ChatMessage latest = chatMessageRepository
                .findTopByRoom_RoomIdOrderBySentAtDesc(myCp.getRoom().getRoomId())
                .orElse(null);

            dtoList.add(ResponseListDmDto.builder()
                .roomId(myCp.getRoom().getRoomId())
                .otherProfileId(other.getProfileId())
                .otherUserNickname(nickname)
                .otherProfileMode(other.getProfileMode().name())
                .latestMessage(latest != null ? latest.getContent() : null)
                .latestMessageAt(latest != null ? latest.getSentAt() : null)
                .status(myCp.getParticipantStatus().name())
                .build());
        }

        return dtoList;
    }

    /**
     * DM 요청 처리
     *
     * @param myProfileId 내 프로필 ID
     * @param myMode      내 프로필 모드
     * @param dto         요청할 상대 프로필 정보
     * @return DM 방 상태 DTO
     */
    public ResponseDmRoomStatusDto requestDm(Long myProfileId, ActiveMode myMode, RequestDmDto dto) {
        // 1. 기존 DM 방 조회 (이미 존재하는지 확인)
        Optional<ChatRoom> existingRoomOpt = chatRoomRepository
            .findDmRoomWithParticipants(myProfileId, myMode, dto.getTargetProfileId(), dto.getTargetProfileMode());

        // 2. 기존 방이 있으면 그대로 사용
        if (existingRoomOpt.isPresent()) {
            ChatRoom room = existingRoomOpt.get();
            ChatParticipant me = findMyParticipant(room.getRoomId(), myProfileId, myMode);
            ChatParticipant other = findOtherParticipant(room.getRoomId(), myProfileId, myMode);

            // 3. 상대방이 WAITING 상태일 경우
            if (other.getParticipantStatus() == ChatParticipant.ParticipantStatus.WAITING) {
                return buildResponseDto(room.getRoomId(), me, other);
            }

            // 4. 상대방이 PENDING 상태일 경우 (수신자가 수락/거절 대기 중)
            if (other.getParticipantStatus() == ChatParticipant.ParticipantStatus.PENDING) {
                return buildResponseDto(room.getRoomId(), me, other);
            }

            // 5. 상대방이 ACCEPTED 상태일 경우 (채팅 가능 상태)
            return buildResponseDto(room.getRoomId(), me, other);
        }

        // 6. 기존 DM 방이 없으면 새 방을 생성
        return createNewDmRoom(myProfileId, myMode, dto);
    }

    // 기존 참여자 찾기
    private ChatParticipant findOtherParticipant(Long roomId, Long myProfileId, ActiveMode myMode) {
        List<ChatParticipant> participants = chatParticipantRepository.findByRoom_RoomId(roomId);
        return participants.stream()
            .filter(p -> !(p.getProfileId().equals(myProfileId) && p.getProfileMode().equals(myMode)))
            .findFirst()
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
    }

    private ChatParticipant findMyParticipant(Long roomId, Long myProfileId, ActiveMode myMode) {
        List<ChatParticipant> participants = chatParticipantRepository.findByRoom_RoomId(roomId);
        return participants.stream()
            .filter(p -> p.getProfileId().equals(myProfileId) && p.getProfileMode().equals(myMode))
            .findFirst()
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
    }

    // 새 DM 방 생성
    private ResponseDmRoomStatusDto createNewDmRoom(Long myProfileId, ActiveMode myMode, RequestDmDto dto) {
        ChatRoom room = ChatRoom.builder()
            .roomType(ChatRoom.RoomType.DM)
            .build();
        chatRoomRepository.save(room);

        ChatParticipant me = ChatParticipant.builder()
            .room(room)
            .profileId(myProfileId)
            .profileMode(myMode)
            .participantStatus(ChatParticipant.ParticipantStatus.WAITING)
            .build();

        ChatParticipant other = ChatParticipant.builder()
            .room(room)
            .profileId(dto.getTargetProfileId())
            .profileMode(dto.getTargetProfileMode())
            .participantStatus(ChatParticipant.ParticipantStatus.WAITING)
            .build();

        chatParticipantRepository.save(me);
        chatParticipantRepository.save(other);

        return buildResponseDto(room.getRoomId(), me, other);
    }

    private ResponseDmRoomStatusDto buildResponseDto(Long roomId, ChatParticipant me, ChatParticipant other) {
        return ResponseDmRoomStatusDto.builder()
            .roomId(roomId)
            .otherProfileId(other.getProfileId())
            .otherProfileMode(other.getProfileMode())
            .myParticipantStatus(me.getParticipantStatus())
            .build();
    }

    /**
     * DM 메시지 전송 -> 최초 전송일 시 송신자의 상태를 ACCEPTED로 변경
     *
     * @param activeProfileId
     * @param activeMode
     * @param request
     * @return
     */
    public ResponseChatMessage sendMessage(Long activeProfileId, ActiveMode activeMode, RequestSendDmDto request) {
        ChatRoom room = chatRoomRepository.findById(request.getRoomId())
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "채팅방 없음"));

        boolean isFirstMessage = chatMessageRepository.countByRoom_RoomId(room.getRoomId()) == 0;

        ChatMessage message = ChatMessage.builder()
            .room(room)
            .profileId(activeProfileId)
            .profileMode(activeMode)
            .content(request.getContent())
            .sentAt(LocalDateTime.now())
            .build();

        chatMessageRepository.save(message);

        if (isFirstMessage) {
            // 1. 송신자 상태 변경 (WAITING → ACCEPTED)
            ChatParticipant senderParticipant = chatParticipantRepository
                .findByRoom_RoomIdAndProfileIdAndProfileMode(
                    room.getRoomId(), activeProfileId, activeMode
                ).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "참가자 정보 없음"));

            senderParticipant.setParticipantStatus(ChatParticipant.ParticipantStatus.ACCEPTED);
            chatParticipantRepository.save(senderParticipant);

            // 2. 상대방 상태 변경 (WAITING → PENDING)
            ChatParticipant recipientParticipant = chatParticipantRepository
                .findWaitingParticipantExcludingSelf(
                    room.getRoomId(),
                    activeProfileId,
                    activeMode,
                    ChatParticipant.ParticipantStatus.WAITING
                ).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "상대방 참가자 정보 없음"));

            recipientParticipant.setParticipantStatus(ChatParticipant.ParticipantStatus.PENDING);
            chatParticipantRepository.save(recipientParticipant);
        }


        return ResponseChatMessage.builder()
            .messageId(message.getMessageId())
            .profileId(message.getProfileId())
            .profileMode(message.getProfileMode().name())
            .senderName(profileService.resolveSenderName(message.getMessageId(), message.getProfileMode()))
            .content(message.getContent())
            .createdAt(message.getCreatedAt())
            .build();
    }


    public String checkUserParticipantStatus(Long roomId, Long activeProfileId, ActiveMode activeMode) {
        ChatParticipant me = chatParticipantRepository
            .RoomRoomIdAndProfileIdAndProfileModeAndParticipantStatusNot(roomId, activeProfileId, activeMode, ChatParticipant.ParticipantStatus.REJECTED)
            .orElseThrow(() -> new ApiException(ErrorCode.FORBIDDEN, "채팅방 접근 불가"));

        return me.getParticipantStatus().name();
    }
}
