package com.encore.encore.domain.chat.repository;

import com.encore.encore.domain.chat.entity.ChatParticipant;
import com.encore.encore.domain.chat.entity.ChatRoom;
import com.encore.encore.domain.member.entity.ActiveMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    List<ChatParticipant> findByRoomRoomId(Long roomId);

    Optional<ChatParticipant> findByRoom_RoomIdAndProfileIdAndProfileModeAndIsDeletedFalse(Long roomId, Long activeId, ActiveMode activeMode);

    List<ChatParticipant> findByRoomRoomIdAndIsDeletedFalse(Long roomId);

    /**
     * 로그인한 사용자의 Pending 상태 DM 참여 목록을 조회합니다.
     *
     * @param profileId         조회할 사용자의 프로필 ID
     * @param participantStatus 조회할 참여 상태 (Pending)
     * @param roomType          조회할 채팅방 타입 (DM)
     * @return 사용자가 참여중인 Pending DM ChatParticipant 리스트
     */
    List<ChatParticipant> findByProfileIdAndParticipantStatusAndRoom_RoomType(
        Long profileId,
        ChatParticipant.ParticipantStatus participantStatus,
        ChatRoom.RoomType roomType
    );

    /**
     * 같은 Room에 속한, 본인과 다른 프로필 역할을 가진 상대방 Participant 조회
     *
     * @param roomId        대상 Room ID
     * @param myProfileId   내 프로필 ID
     * @param myProfileMode 내 프로필 역할
     * @return 상대방 ChatParticipant
     */
    @Query("""
            SELECT cp
            FROM ChatParticipant cp
            WHERE cp.room.roomId = :roomId
              AND cp.profileId <> :myProfileId
              AND cp.profileMode <> :myProfileMode
        """)
    Optional<ChatParticipant> findOtherParticipantInRoom(
        @Param("roomId") Long roomId,
        @Param("myProfileId") Long myProfileId,
        @Param("myProfileMode") ActiveMode myProfileMode
    );


    List<ChatParticipant> findByProfileIdAndProfileModeAndParticipantStatusAndRoom_RoomType(Long activeProfileId, ActiveMode activeMode, ChatParticipant.ParticipantStatus participantStatus, ChatRoom.RoomType roomType);
}
