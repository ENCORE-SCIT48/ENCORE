package com.encore.encore.domain.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.encore.encore.domain.community.entity.PostInteraction;

public interface PostInteractionRepository extends JpaRepository<PostInteraction, Long>{
    
}
