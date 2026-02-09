package com.encore.encore.domain.community.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.encore.encore.domain.community.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long>{
    
    /**
     * 논리 삭제되지 않은 게시글을 페이징 조회합니다.
     *
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    Page<Post> findByIsDeletedFalse(Pageable pageable);

    /**
     * 특정 postType의 게시글을 논리 삭제 제외 후 페이징 조회합니다.
     *
     * @param postType 게시글 타입
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    Page<Post> findByPostTypeAndIsDeletedFalse(String postType, Pageable pageable);
}
