package com.encore.encore.domain.chat.repository;

import com.encore.encore.domain.chat.entity.ChatParticipant;
import com.encore.encore.domain.member.entity.ActiveMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {


    boolean existsByRoomRoomIdAndProfileIdAndProfileMode(Long roomId, Long activeId, ActiveMode activeMode);
}
