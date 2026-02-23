package com.encore.encore.domain.community.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.encore.encore.domain.community.entity.Post;

import java.util.List;
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

    /**
     * [설명] 특정 공연자(performerId)가 작성한 공연자 모집 게시글을 조회합니다.
     *
     * - performerAuthor의 performerId를 기준으로 조회합니다.
     * - postType이 PERFORMER_RECRUIT인 게시글만 조회합니다.
     * - 논리 삭제(isDeleted=false)된 게시글만 조회합니다.
     *
     * @param performerId 공연자 ID
     * @param postType    게시글 타입 (예: PERFORMER_RECRUIT)
     * @param pageable    페이징 정보
     * @return 공연자 모집 게시글 페이지
     */
    Page<Post> findByPerformerAuthor_PerformerIdAndPostTypeAndIsDeletedFalse(
            Long performerId,
            String postType,
            Pageable pageable);

    /**
     * [설명] 특정 공연자(performerId)가 작성한 게시글을 조회합니다.
     *
     * - performerAuthor의 performerId 기준으로 조회합니다.
     * - 특정 postType에 해당하는 게시글만 조회합니다.
     * - 논리 삭제되지 않은 게시글만 조회합니다.
     *
     * @param performerId 공연자 ID
     * @param postType    게시글 타입 (예: PERFORMANCE_RECRUIT)
     * @return 게시글 목록
     */
    List<Post> findByPerformerAuthor_PerformerIdAndPostTypeAndIsDeletedFalse(
            Long performerId,
            String postType);
}
