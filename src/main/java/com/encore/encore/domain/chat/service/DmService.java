package com.encore.encore.domain.chat.service;

import com.encore.encore.domain.chat.dto.ResponseChatMessage;
import com.encore.encore.domain.chat.dto.dm.*;
import com.encore.encore.domain.chat.entity.ChatMessage;
import com.encore.encore.domain.chat.entity.ChatParticipant;
import com.encore.encore.domain.chat.entity.ChatRoom;
import com.encore.encore.domain.chat.repository.ChatMessageRepository;
import com.encore.encore.domain.chat.repository.ChatParticipantRepository;
import com.encore.encore.domain.chat.repository.ChatRoomRepository;
import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.member.repository.HostProfileRepository;
import com.encore.encore.domain.member.repository.PerformerProfileRepository;
import com.encore.encore.domain.member.repository.UserProfileRepository;
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
    private final UserProfileRepository userProfileRepository;
    private final PerformerProfileRepository performerProfileRepository;
    private final HostProfileRepository hostProfileRepository;

    /**
     * String으로 받은 activeMode를 ActiveMode로 전환
     *
     * @param modeStr
     * @return
     */
    private ActiveMode mapToActiveMode(String modeStr) {
        if (modeStr == null || modeStr.isBlank()) {
            throw new ApiException(ErrorCode.NOT_FOUND, "ActiveMode가 비어있습니다.");
        }

        switch (modeStr.toLowerCase()) { // 소문자로 통일
            case "user":
                return ActiveMode.USER;
            case "performer":
                return ActiveMode.PERFORMER;
            case "host":
                return ActiveMode.HOST;
            default:
                throw new ApiException(ErrorCode.INVALID_REQUEST, "잘못된 ActiveMode: " + modeStr);
        }
    }

    /**
     * 대상 프로필이 존재하는 프로필인지 확인
     *
     * @param profileId
     * @param mode
     * @return
     */
    private boolean isProfileExist(Long profileId, ActiveMode mode) {
        switch (mode) {
            case USER:
                return userProfileRepository.existsById(profileId);
            case PERFORMER:
                return performerProfileRepository.existsById(profileId);
            case HOST:
                return hostProfileRepository.existsById(profileId);
            default:
                return false;
        }
    }


    /**
     * 로그인한 사용자의 Pending 상태인 DM 참여 내역을 조회합니다.
     *
     * <p>메서드 동작:
     * <ul>
     *     <li>로그인한 사용자의 프로필 ID와 프로필 모드에 해당하는 Pending DM 참여 목록을 조회합니다.</li>
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
            .findAcceptedDm(
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
            .findAcceptedDm(
                activeProfileId,
                activeMode,
                ChatParticipant.ParticipantStatus.ACCEPTED,  // pending -> accepted
                ChatRoom.RoomType.DM
            );

        List<ResponseListDmDto> dtoList = new ArrayList<>();

        for (ChatParticipant myCp : myAcceptedList) {

            // 상대방 조회: 나와 profileId, profileMode 모두 다른 사람
            Optional<ChatParticipant> otherOpt = chatParticipantRepository.findOtherParticipantInRoom(
                myCp.getRoom().getRoomId(),
                activeProfileId,
                activeMode
            );

            if (otherOpt.isEmpty()) {
                log.warn("상대방 참가자가 없는 DM 방입니다. roomId={}", myCp.getRoom().getRoomId());
                continue; // dtoList에 추가하지 않고 다음 루프로 넘어감
            }

            ChatParticipant other = otherOpt.get();

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

        ActiveMode targetProfileMode = mapToActiveMode(dto.getTargetProfileMode());

        if (myProfileId.equals(dto.getTargetProfileId()) &&
            myMode == targetProfileMode) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "자기 자신에게 DM을 보낼 수 없습니다.");
        }

        if (!isProfileExist(dto.getTargetProfileId(), targetProfileMode)) {
            throw new ApiException(ErrorCode.NOT_FOUND, "존재하지 않는 프로필입니다.");
        }

        // 1. 기존 DM 방 조회 (이미 존재하는지 확인)
        Optional<ChatRoom> existingRoomOpt = chatRoomRepository
            .findDmRoomWithParticipants(myProfileId, myMode, dto.getTargetProfileId(), targetProfileMode);

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

        ActiveMode targetProfileMode = mapToActiveMode(dto.getTargetProfileMode());
        ChatParticipant other = ChatParticipant.builder()
            .room(room)
            .profileId(dto.getTargetProfileId())
            .profileMode(targetProfileMode)
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
            .otherProfileMode(other.getProfileMode().name())
            .myParticipantStatus(me.getParticipantStatus().name())
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
    public ResponseSendDmDto sendMessage(Long activeProfileId, ActiveMode activeMode, RequestSendDmDto request) {
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


        return ResponseSendDmDto.builder()
            .roomId(message.getRoom().getRoomId())
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

    public List<ResponseChatMessage> getMessages(Long roomId, int page, int size) {

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
                .build()
            )
            .toList();
    }

    /**
     * DM 수락/변경에 따라 참여자의 상태를 변화시킵니다.
     *
     * @param roomId
     * @param dto
     * @param activeProfileId
     * @param activeMode
     * @return
     */
    public ResponseUpdateDmStatusDto handleRoomStatus(
        Long roomId, RequestDmStatusDto dto, Long activeProfileId, ActiveMode activeMode) {

        ChatParticipant chatParticipant =
            chatParticipantRepository.findByProfileIdAndProfileModeAndRoomRoomId(activeProfileId, activeMode, roomId)
                .orElseThrow(
                    () -> new ApiException(ErrorCode.NOT_FOUND, "참여자가 존재하지 않습니다.")
                );
        String statusStr = dto.getStatus(); // "ACCEPTED" 또는 "REJECTED"

        // 2. 상태에 따른 로직 분기 (문자열 비교 시 대소문자 주의)
        if ("ACCEPTED".equalsIgnoreCase(statusStr)) {
            log.info("[INFO] 참가자 수락 처리 - ProfileId: {}, RoomId: {}", activeProfileId, roomId);
            chatParticipant.setParticipantStatus(ChatParticipant.ParticipantStatus.ACCEPTED);
            // 수락 시 추가 로직이 있다면 여기에 작성
        } else if ("REJECTED".equalsIgnoreCase(statusStr)) {
            log.info("[INFO] 참가자 거절 및 논리 삭제 처리 - ProfileId: {}, RoomId: {}", activeProfileId, roomId);
            chatParticipant.setParticipantStatus(ChatParticipant.ParticipantStatus.REJECTED);

            ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(
                    () -> new ApiException(ErrorCode.NOT_FOUND, "채팅방이 존재하지 않습니다.")
                );
            chatRoom.delete();
        } else {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "잘못된 상태 값입니다.");
        }

        // 3. 결과 반환 (Response DTO 생성)
        return ResponseUpdateDmStatusDto.builder()
            .roomId(roomId)
            .status(chatParticipant.getParticipantStatus().name())
            .profileId(activeProfileId)
            .profileMode(activeMode.name())
            .build();
    }
}
