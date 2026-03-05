package com.encore.encore.domain.chat.repository;

import com.encore.encore.domain.chat.entity.ChatRoom;
import com.encore.encore.domain.member.entity.ActiveMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    ChatRoom findByChatPost_Id(Long postId);


    /**
     * 주어진 두 참가자가 모두 참여한 1:1 DM(ChatRoom.RoomType.DM) 방을 조회합니다.
     *
     * <p>조건:
     * <ul>
     *     <li>roomType이 DM이어야 함</li>
     *     <li>p1은 나의 프로필 ID와 모드에 일치</li>
     *     <li>p2는 상대방 프로필 ID와 모드에 일치</li>
     * </ul>
     * </p>
     *
     * @param myProfileId       조회하는 나의 프로필 ID
     * @param myMode            조회하는 나의 프로필 모드 (ActiveMode)
     * @param targetProfileId   상대방 프로필 ID
     * @param targetProfileMode 상대방 프로필 모드 (ActiveMode)
     * @return 두 참가자가 모두 참여한 DM 방이 존재하면 Optional에 담아 반환,
     * 존재하지 않으면 Optional.empty()
     */
    @Query("""
            SELECT r FROM ChatRoom r
            JOIN ChatParticipant p1 ON p1.room = r
            JOIN ChatParticipant p2 ON p2.room = r
            WHERE r.roomType = 'DM'
            AND p1.profileId = :myId AND p1.profileMode = :myMode
            AND p2.profileId = :targetId AND p2.profileMode = :targetMode
            AND r.isDeleted = false
        """)
    Optional<ChatRoom> findDmRoomWithParticipants(
        @Param("myId") Long myId,
        @Param("myMode") ActiveMode myMode,
        @Param("targetId") Long targetId,
        @Param("targetMode") ActiveMode targetMode
    );


}
