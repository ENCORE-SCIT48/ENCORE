package com.encore.encore.domain.community.service;

import com.encore.encore.domain.community.dto.RequestCreatePostDto;
import com.encore.encore.domain.community.dto.ResponseCreatePostDto;
import com.encore.encore.domain.community.dto.ResponseDeletePostDto;
import com.encore.encore.domain.community.dto.ResponseListPostDto;
import com.encore.encore.domain.community.dto.ResponseReadPostDto;
import com.encore.encore.domain.community.entity.Post;
import com.encore.encore.domain.community.repository.PostRepository;
import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.domain.performance.repository.PerformanceRepository;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final PerformanceRepository performanceRepository;

    /**
     * [설명] 공연자 모집 게시글을 등록합니다.
     *
     * @param dto 게시글 등록 요청 객체
     * @return 등록된 게시글 정보
     */
    public ResponseCreatePostDto createPost(RequestCreatePostDto dto) {
        log.info("게시글 등록 시작 - postType={}", dto.getPostType());

        Performance performance = null;
        if (dto.getPerformanceId() != null) {
            performance = performanceRepository.findById(dto.getPerformanceId())
                    .orElseThrow(() -> {
                        log.error("공연 정보 없음 - performanceId={}", dto.getPerformanceId());
                        return new ApiException(ErrorCode.NOT_FOUND, "공연 정보를 찾을 수 없습니다.");
                    });
        }

        Post post = Post.builder()
                .performance(performance)
                .postType(dto.getPostType())
                .title(dto.getTitle())
                .content(dto.getContent())
                .viewCount(0)
                .build();

        Post savedPost = postRepository.save(post);

        log.info("게시글 등록 완료 - postId={}", savedPost.getPostId());

        return ResponseCreatePostDto.builder()
                .postId(savedPost.getPostId())
                .postType(savedPost.getPostType())
                .title(savedPost.getTitle())
                .createdAt(savedPost.getCreatedAt())
                .build();
    }

    /**
     * [설명] 공연자 모집 게시글을 논리 삭제합니다.
     *
     * @param postId 삭제할 게시글 ID
     * @return 게시글 삭제 결과
     */
    public ResponseDeletePostDto deletePost(Long postId) {

        log.info("게시글 삭제 요청 - postId={}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("게시글 없음 - postId={}", postId);
                    return new ApiException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다.");
                });

        if (post.isDeleted()) {
            log.error("이미 삭제된 게시글 - postId={}", postId);
            throw new ApiException(ErrorCode.INVALID_REQUEST, "이미 삭제된 게시글입니다.");
        }

        /**
         * Why:
         * 물리 삭제는 데이터 복구 및 이력 관리가 불가능하므로
         * 서비스 정책상 논리 삭제를 사용한다.
         */
        post.delete();

        log.info("게시글 삭제 완료 - postId={}", postId);

        return ResponseDeletePostDto.builder()
                .postId(postId)
                .deleted(true)
                .build();
    }

    /**
     * [설명] 공연자 모집 게시글 단건 상세 정보를 조회합니다.
     * 조회 시 조회수를 1 증가시킵니다.
     *
     * @param postId 게시글 ID
     * @return 게시글 상세 정보
     */
    public ResponseReadPostDto readPost(Long postId) {
        log.info("게시글 단건 조회 요청 - postId={}", postId);

        Post post;
        try {
            post = postRepository.findById(postId).orElseThrow();
        } catch (Exception e) {
            log.error("게시글 조회 실패 - postId={}", postId, e);
            throw new ApiException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }

        if (post.isDeleted()) {
            ApiException ex = new ApiException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다.");
            log.error("삭제된 게시글 접근 - postId={}", postId, ex);
            throw ex;
        }

        /**
         * Why:
         * 단건 상세 조회는 실제 사용자 열람 행위이므로
         * 이 시점에 조회수를 증가시킨다.
         */
        post.setViewCount(post.getViewCount() + 1);

        return ResponseReadPostDto.builder()
                .postId(post.getPostId())
                .postType(post.getPostType())
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .build();
    }

    /**
     * [설명] 공연자 모집 게시글 목록을 페이징 조회합니다.
     * 삭제되지 않은 게시글만 조회 대상입니다.
     *
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    @Transactional(readOnly = true)
    public Page<ResponseListPostDto> listPosts(Pageable pageable) {
        log.info(
                "게시글 목록 페이징 조회 요청 - page={}, size={}",
                pageable.getPageNumber(),
                pageable.getPageSize());

        return postRepository.findByIsDeletedFalse(pageable)
                .map(post -> ResponseListPostDto.builder()
                        .postId(post.getPostId())
                        .postType(post.getPostType())
                        .title(post.getTitle())
                        .viewCount(post.getViewCount())
                        .createdAt(post.getCreatedAt())
                        .build());
    }

    /**
     * [설명] 특정 postType의 공연자 모집 게시글을 페이징 조회합니다.
     *
     * @param postType 게시글 타입
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    @Transactional(readOnly = true)
    public Page<ResponseListPostDto> listPostsByType(String postType, Pageable pageable) {
        log.info(
                "게시글 목록 페이징 조회 요청 - postType={}, page={}, size={}",
                postType,
                pageable.getPageNumber(),
                pageable.getPageSize());

        return postRepository.findByPostTypeAndIsDeletedFalse(postType, pageable)
                .map(post -> ResponseListPostDto.builder()
                        .postId(post.getPostId())
                        .postType(post.getPostType())
                        .title(post.getTitle())
                        .viewCount(post.getViewCount())
                        .createdAt(post.getCreatedAt())
                        .build());
    }
}
