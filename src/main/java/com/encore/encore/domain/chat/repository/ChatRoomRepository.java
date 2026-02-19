package com.encore.encore.domain.chat.repository;

import com.encore.encore.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    
    ChatRoom findByChatPost_Id(Long postId);
}
