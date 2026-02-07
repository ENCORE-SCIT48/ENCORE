package com.encore.encore.domain.chat.repository;


import com.encore.encore.domain.chat.dto.ResponseListChatPostDto;
import com.encore.encore.domain.chat.entity.ChatPost;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatPostRepository extends JpaRepository<ChatPost, Long> {
    List<ChatPost> findByPerformance_PerformanceId(Long performanceId);

    /**
     * PERFORMANCE 별 채팅방 제목/제목+내용 검색
     *
     * @param performanceId
     * @param keyword
     * @param searchType
     * @param onlyOpen
     * @param pageable
     * @return
     */
    @Query("""
            SELECT new com.encore.encore.domain.chat.dto.ResponseListChatPostDto(
                p.id, p.title, p.status, p.currentMember, p.maxMember, p.updatedAt
            )
            FROM ChatPost p
            JOIN ChatRoom r ON r.chatPost.id = p.id
            WHERE p.performance.id = :performanceId
              AND r.roomType = com.encore.encore.domain.chat.entity.ChatRoom.RoomType.CHAT
              AND p.isDeleted = false
              AND r.isDeleted = false
              AND (:keyword IS NULL OR
                   (:searchType = 'title' AND p.title LIKE CONCAT('%', :keyword, '%')) OR
                   (:searchType = 'titleContent' AND (p.title LIKE CONCAT('%', :keyword, '%') OR p.content LIKE CONCAT('%', :keyword, '%')))
              )
              AND (:onlyOpen = false OR p.status = com.encore.encore.domain.chat.entity.ChatPost.Status.OPEN)
            ORDER BY p.updatedAt DESC
        """)
    Slice<ResponseListChatPostDto> findChatPostList(
        @Param("performanceId") Long performanceId,
        @Param("keyword") String keyword,
        @Param("searchType") String searchType,
        @Param("onlyOpen") boolean onlyOpen,
        Pageable pageable);


    /**
     * 전체 채팅방 조회
     *
     * @param performanceId
     * @return
     */
    @Query("SELECT p FROM ChatPost p " +
        "JOIN ChatRoom r ON r.chatPost = p " +
        "WHERE r.roomType = 'PERFORMANCE_ALL' " +
        "AND p.performance.id = :performanceId")
    Optional<ChatPost> findPerformanceAllPost(@Param("performanceId") Long performanceId);

    /**
     * 자신이 참여한 채팅방 중 최신 갱신 순 채팅방 조회
     *
     * @param userId
     * @param limit
     * @return
     */
    @Query(value = """
            SELECT p.*
            FROM chat_post p
            JOIN chat_room r ON r.post_id = p.id
            JOIN chat_participant cp ON cp.room_id = r.room_id
            LEFT JOIN chat_message m ON m.room_id = r.room_id
            WHERE cp.user_id = :userId
            GROUP BY p.id
            ORDER BY COALESCE(MAX(m.sent_at), p.updated_at) DESC
            LIMIT :limit
        """, nativeQuery = true)
    List<ChatPost> findTopParticipatingByUserIdNative(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 채팅방 타입이 CHAT,PERFORMANCE_ALL중 가장 최근에 메시지가 보내진 채팅방 조회
     *
     * @param limit
     * @return
     */
    @Query(value = """
            SELECT p.*
            FROM chat_post p
            JOIN chat_room r ON r.post_id = p.id
            LEFT JOIN chat_message m ON m.room_id = r.room_id
            WHERE r.room_type IN ('CHAT', 'PERFORMANCE_ALL')
            GROUP BY p.id
            ORDER BY COALESCE(MAX(m.sent_at), p.updated_at) DESC
            LIMIT :limit
        """, nativeQuery = true)
    List<ChatPost> findHotChatPosts(@Param("limit") int limit);

    /**
     * 자신이 참여한 채팅방 무한스크롤 조회
     *
     * @param userId
     * @param pageable
     * @return
     */

    @Query(value = """
            SELECT p.*
            FROM chat_post p
            JOIN chat_room r ON r.post_id = p.id
            JOIN chat_participant cp ON cp.room_id = r.room_id
            LEFT JOIN chat_message m ON m.room_id = r.room_id
            WHERE cp.user_id = :userId
              AND (
                     (:keyword IS NULL OR :keyword = '')
                     OR (:searchType = 'title' AND p.title LIKE CONCAT('%', :keyword, '%'))
                     OR (:searchType = 'titleContent' AND (p.title LIKE CONCAT('%', :keyword, '%') OR p.content LIKE CONCAT('%', :keyword, '%')))
                     OR (:searchType = 'performanceTitle' AND p.performance_id IN (
                         /* 여기 perf.id 대신 실제 DB 컬럼명을 넣어야 합니다 */
                         SELECT perf.performance_id FROM performance perf
                         WHERE perf.title LIKE CONCAT('%', :keyword, '%')
                     ))
                 )
            GROUP BY p.id
            ORDER BY COALESCE(MAX(m.sent_at), p.updated_at) DESC
            LIMIT :size OFFSET :offset
        """, nativeQuery = true)
    List<ChatPost> findJoinedChats(
        @Param("userId") Long userId,
        @Param("keyword") String keyword, // performance 검색 시 ID 값이 문자열로 들어온다고 가정
        @Param("searchType") String searchType,
        @Param("size") int size,
        @Param("offset") int offset
    );

}
