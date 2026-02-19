package com.encore.encore.domain.member.repository;

import com.encore.encore.domain.member.entity.HostProfile;
import com.encore.encore.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HostProfileRepository extends JpaRepository<HostProfile, Long> {

    Optional<HostProfile> findByUser_UserId(Long userId);
    Optional<HostProfile> findByUser(User user);
}
