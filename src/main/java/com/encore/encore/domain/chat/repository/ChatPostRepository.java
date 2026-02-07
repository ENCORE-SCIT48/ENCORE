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
     * 특정 공연에 연결된 'PERFORMANCE_ALL' 타입 채팅방(Post)을 조회한다.
     *
     * <p>
     * 조회 대상은 {@code ChatRoom}이 {@code PERFORMANCE_ALL} 타입이며,
     * 연결된 {@code ChatPost}가 주어진 공연 ID와 일치하는 경우이다.
     * </p>
     *
     * @param performanceId 조회할 공연 ID
     * @return 주어진 공연과 연결된 {@code PERFORMANCE_ALL} 채팅방 {@code ChatPost}, 존재하지 않으면 {@link Optional#empty()}
     */
    @Query("SELECT p FROM ChatPost p " +
        "JOIN ChatRoom r ON r.chatPost = p " +
        "WHERE r.roomType = 'PERFORMANCE_ALL' " +
        "AND p.performance.id = :performanceId")
    Optional<ChatPost> findPerformanceAllPost(@Param("performanceId") Long performanceId);

    /**
     * 특정 사용자가 참여 중인 채팅방 중 최근 활동 순으로 상위 N개를 조회한다.
     *
     * <p>
     * 조회 대상은 참여자로 등록된 채팅방이며,
     * 가장 최근에 메시지가 보내진 순으로 정렬된다.
     * 메시지가 없는 채팅방은 채팅방 수정 시간({@code updated_at}) 기준으로 정렬된다.
     * </p>
     *
     * <p>
     * 조회 개수는 {@code limit} 파라미터로 제한된다.
     * </p>
     *
     * @param userId 조회할 사용자의 ID
     * @param limit  조회할 채팅방 개수 제한
     * @return 사용자가 참여 중인 채팅방 중 최근 활동 순으로 정렬된 목록
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
     * 최근 활동이 많은 채팅방(Hot Chat)을 조회한다.
     *
     * <p>
     * 조회 대상 채팅방은 {@code CHAT} 또는 {@code PERFORMANCE_ALL} 타입의 채팅방이며,
     * 가장 최근에 메시지가 보내진 순으로 정렬된다.
     * 메시지가 없는 채팅방은 수정 시간({@code updated_at})을 기준으로 정렬된다.
     * </p>
     *
     * <p>
     * 조회 개수는 {@code limit} 파라미터로 제한한다.
     * </p>
     *
     * @param limit 조회할 채팅방 개수 제한
     * @return 최근 활동 순으로 정렬된 채팅방 목록
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
     * 사용자가 참여 중인 채팅방 목록을 조회한다.
     *
     * <p>
     * 채팅방은 사용자가 참여자로 등록된 채팅방만 조회되며,
     * 검색어와 검색 타입에 따라 다음 기준으로 필터링할 수 있다.
     * </p>
     *
     * <ul>
     *   <li>title : 채팅방 제목 기준 검색</li>
     *   <li>titleContent : 채팅방 제목 + 내용 기준 검색</li>
     *   <li>performanceTitle : 연결된 공연 제목 기준 검색</li>
     * </ul>
     *
     * <p>
     * 채팅방 목록은 가장 최근 메시지 전송 시각을 기준으로 내림차순 정렬되며,
     * 메시지가 없는 경우 채팅방 수정 시각을 기준으로 정렬된다.
     * </p>
     *
     * @param userId     참여 중인 채팅방을 조회할 사용자 ID
     * @param keyword    검색어 (null 또는 빈 문자열일 경우 검색 조건 미적용)
     * @param searchType 검색 기준
     *                   (title / titleContent / performanceTitle)
     * @param size       조회할 채팅방 개수 (LIMIT)
     * @param offset     조회 시작 위치 (OFFSET)
     * @return 사용자가 참여 중인 채팅방 목록
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
        @Param("keyword") String keyword,
        @Param("searchType") String searchType,
        @Param("size") int size,
        @Param("offset") int offset
    );

}
