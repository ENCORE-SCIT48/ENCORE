package com.encore.encore.domain.member.repository;

import com.encore.encore.domain.member.entity.HostProfile;
import com.encore.encore.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HostProfileRepository extends JpaRepository<HostProfile, Long> {

    Optional<HostProfile> findByUser_UserId(Long userId);
    Optional<HostProfile> findByUser(User user);
    boolean existsByUser(User user);   // 공연자 프로필 존재 여부 체크 (접근 검증용)
}
