package com.encore.encore.domain.chat.repository;

import com.encore.encore.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRoomRoomIdOrderByCreatedAtAsc(Long roomId);
    
    Optional<ChatMessage> findTopByRoom_RoomIdOrderBySentAtDesc(Long roomId);
    
    int countByRoom_RoomId(Long roomId);
}
