package com.encore.encore.domain.community.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.encore.encore.domain.community.entity.Post;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

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

    /**
     * 공연자 모집글 단건 조회 (논리 삭제 제외)
     *
     * @param postId   게시글 ID
     * @param postType 게시글 타입
     * @return 게시글
     */
    Optional<Post> findByPostIdAndPostTypeAndIsDeletedFalse(
            Long postId,
            String postType);

    /**
     * 특정 postType + 제목 검색 + 논리 삭제 제외 후 페이징 조회합니다.
     *
     * @param postType 게시글 타입
     * @param title    검색어(제목)
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    Page<Post> findByPostTypeAndTitleContainingIgnoreCaseAndIsDeletedFalse(
            String postType,
            String title,
            Pageable pageable);
}
