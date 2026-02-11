package com.encore.encore.domain.chat.service;

import com.encore.encore.domain.chat.dto.dm.ResponseListDmDto;
import com.encore.encore.domain.chat.entity.ChatMessage;
import com.encore.encore.domain.chat.entity.ChatParticipant;
import com.encore.encore.domain.chat.entity.ChatRoom;
import com.encore.encore.domain.chat.repository.ChatMessageRepository;
import com.encore.encore.domain.chat.repository.ChatParticipantRepository;
import com.encore.encore.domain.chat.repository.ChatRoomRepository;
import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.member.service.ProfileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
    public List<ResponseListDmDto> getPending(Long activeProfileId, ActiveMode activeMode) {
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
            ).orElseThrow(() -> new RuntimeException("상대방 참가자가 없습니다."));

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


}
