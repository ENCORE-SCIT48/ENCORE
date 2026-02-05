package com.encore.encore.domain.member.repository;

import com.encore.encore.domain.member.entity.HostProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HostProfileRepository extends JpaRepository<HostProfile, Long> {
}
