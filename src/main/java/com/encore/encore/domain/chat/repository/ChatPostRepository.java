package com.encore.encore.domain.chat.repository;


import com.encore.encore.domain.chat.entity.ChatPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatPostRepository extends JpaRepository<ChatPost, Long> {
}
