package com.encore.encore.domain.user.repository;

import com.encore.encore.domain.user.entity.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
}
