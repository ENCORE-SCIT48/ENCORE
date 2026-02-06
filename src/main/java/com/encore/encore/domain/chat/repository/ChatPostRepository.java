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


    @Query("SELECT p FROM ChatPost p " +
        "JOIN ChatRoom r ON r.chatPost = p " +
        "WHERE r.roomType = 'PERFORMANCE_ALL' " +
        "AND p.performance.id = :performanceId")
    Optional<ChatPost> findPerformanceAllPost(@Param("performanceId") Long performanceId);

}
