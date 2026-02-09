package com.encore.encore.domain.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.encore.encore.domain.community.entity.Recommendation;

public interface RecomendationRepository extends JpaRepository<Recommendation, Long>{
    
}
