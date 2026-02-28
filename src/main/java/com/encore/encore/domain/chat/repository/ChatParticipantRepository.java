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
     * @param roomId      대상 Room ID
     * @param profileId   내 프로필 ID
     * @param profileMode 내 프로필 역할
     * @return 상대방 ChatParticipant
     */
    @Query("""
            SELECT cp
            FROM ChatParticipant cp
            WHERE cp.room.roomId = :roomId
              AND NOT (cp.profileId = :profileId AND cp.profileMode = :profileMode)
        """)
    Optional<ChatParticipant> findOtherParticipantInRoom(
        @Param("roomId") Long roomId,
        @Param("profileId") Long profileId,
        @Param("profileMode") ActiveMode profileMode
    );
    
    
    @Query("""
            SELECT cp
            FROM ChatParticipant cp
            JOIN cp.room r
            WHERE cp.profileId = :profileId
              AND cp.profileMode = :profileMode
              AND cp.participantStatus = :status
              AND r.roomType = :roomType
        """)
    List<ChatParticipant> findAcceptedDm(
        @Param("profileId") Long profileId,
        @Param("profileMode") ActiveMode profileMode,
        @Param("status") ChatParticipant.ParticipantStatus status,
        @Param("roomType") ChatRoom.RoomType roomType
    );
    
    List<ChatParticipant> findByRoom_RoomId(Long roomId);
    
    Optional<ChatParticipant> findByRoom_RoomIdAndProfileIdAndProfileMode(Long roomId, Long activeProfileId, ActiveMode activeMode);
    
    Optional<ChatParticipant> RoomRoomIdAndProfileIdAndProfileModeAndParticipantStatusNot
        (Long roomId, Long activeProfileId, ActiveMode activeMode, ChatParticipant.ParticipantStatus status);
    
    @Query("SELECT p FROM ChatParticipant p " +
        "WHERE p.room.roomId = :roomId " +
        "AND p.participantStatus = :status " +
        "AND (p.profileId <> :profileId OR p.profileMode <> :profileMode)")
    Optional<ChatParticipant> findWaitingParticipantExcludingSelf(
        @Param("roomId") Long roomId,
        @Param("profileId") Long profileId,
        @Param("profileMode") ActiveMode profileMode,
        @Param("status") ChatParticipant.ParticipantStatus status
    );
    
    /**
     * [설명] 프로필 ID, 프로필 모드, 방 ID를 조건으로 채팅 참가자 정보를 조회합니다.
     *
     * @param profileId   사용자 프로필 식별자
     * @param profileMode 프로필 모드 (예: USER, ADMIN 등)
     * @param roomId      채팅방 식별자
     * @return 검색된 참가자 정보 (Optional로 감싸서 Null 안전성 확보)
     */
    Optional<ChatParticipant> findByProfileIdAndProfileModeAndRoomRoomId(Long profileId, ActiveMode profileMode, Long roomId);
    
    List<ChatParticipant> findAllByRoom_RoomId(Long roomId);
}
