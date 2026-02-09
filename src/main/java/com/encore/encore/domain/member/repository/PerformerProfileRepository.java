package com.encore.encore.domain.member.repository;

import com.encore.encore.domain.member.entity.PerformerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerformerProfileRepository extends JpaRepository<PerformerProfile, Long> {
    // UserProfileRepository.java
    Optional<PerformerProfile> findByUser_UserId(Long userId);
}
