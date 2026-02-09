package com.encore.encore.domain.member.repository;

import com.encore.encore.domain.member.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    // UserProfileRepository.java
    Optional<UserProfile> findByUser_UserId(Long userId);
}
