package com.encore.encore.domain.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.encore.encore.domain.community.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long>{
    
}
