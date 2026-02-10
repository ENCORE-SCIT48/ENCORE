package com.encore.encore.domain.chat.repository;

import com.encore.encore.domain.chat.entity.ChatParticipant;
import com.encore.encore.domain.member.entity.ActiveMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {


    boolean existsByRoomRoomIdAndProfileIdAndProfileMode(Long roomId, Long activeId, ActiveMode activeMode);

    List<ChatParticipant> findByRoomRoomId(Long roomId);

    Optional<ChatParticipant> findByRoom_RoomIdAndProfileIdAndProfileModeAndIsDeletedFalse(Long roomId, Long activeId, ActiveMode activeMode);
}
