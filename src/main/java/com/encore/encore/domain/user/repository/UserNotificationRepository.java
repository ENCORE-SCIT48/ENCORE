package com.encore.encore.domain.user.repository;

import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.user.entity.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    Optional<UserNotification> findByUser_UserIdAndProfileMode(Long userId, ActiveMode profileMode);
}
