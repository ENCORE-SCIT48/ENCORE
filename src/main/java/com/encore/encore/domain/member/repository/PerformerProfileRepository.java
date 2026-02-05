package com.encore.encore.domain.member.repository;

import com.encore.encore.domain.member.entity.PerformerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerformerProfileRepository extends JpaRepository<PerformerProfile, Long> {
}
