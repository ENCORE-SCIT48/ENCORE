package com.encore.encore.domain.chat.repository;


import com.encore.encore.domain.chat.entity.ChatPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatPostRepository extends JpaRepository<ChatPost, Long> {
    List<ChatPost> findByPerformance_PerformanceId(Long performanceId);
}
