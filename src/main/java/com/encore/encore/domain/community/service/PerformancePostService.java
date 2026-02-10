package com.encore.encore.domain.community.service;

import com.encore.encore.domain.community.dto.PerformancePostDto.*;
import com.encore.encore.domain.community.entity.Post;
import com.encore.encore.domain.community.repository.PostRepository;
import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.domain.performance.repository.PerformanceRepository;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PerformancePostService {

    private static final String PERFORMANCE_POST_TYPE = "PERFORMANCE_RECRUIT";

    private final PostRepository postRepository;
    private final PerformanceRepository performanceRepository;

    /**
     * [설명] 공연 모집 게시글을 등록합니다.
     *
     * @param dto 게시글 등록 요청 객체
     * @return 등록된 게시글 정보
     */
    public ResponseCreatePerformancePostDto createPerformancePost(
        RequestCreatePerformancePostDto dto
    ) {
        log.info("공연 모집 게시글 등록 시작");

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
            .postType(PERFORMANCE_POST_TYPE)
            .title(dto.getTitle())
            .content(dto.getContent())
            .viewCount(0)
            .build();

        Post savedPost = postRepository.save(post);

        log.info("공연 모집 게시글 등록 완료 - postId={}", savedPost.getPostId());

        return ResponseCreatePerformancePostDto.builder()
            .postId(savedPost.getPostId())
            .postType(savedPost.getPostType())
            .title(savedPost.getTitle())
            .createdAt(savedPost.getCreatedAt())
            .build();
    }

    /**
     * [설명] 공연 모집 게시글을 논리 삭제합니다.
     *
     * @param postId 삭제할 게시글 ID
     * @return 게시글 삭제 결과
     */
    public ResponseDeletePerformancePostDto deletePerformancePost(Long postId) {
        log.info("공연 모집 게시글 삭제 요청 - postId={}", postId);

        Post post = postRepository
            .findByPostIdAndPostTypeAndIsDeletedFalse(postId, PERFORMANCE_POST_TYPE)
            .orElseThrow(() -> {
                log.error("삭제 대상 게시글 없음 - postId={}", postId);
                return new ApiException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다.");
            });

        post.delete();

        log.info("공연 모집 게시글 삭제 완료 - postId={}", postId);

        return ResponseDeletePerformancePostDto.builder()
            .postId(postId)
            .deleted(true)
            .build();
    }

    /**
     * [설명] 공연 모집 게시글 단건 상세 정보를 조회합니다.
     * 조회 시 조회수가 증가합니다.
     *
     * @param postId 게시글 ID
     * @return 게시글 상세 정보
     */
    public ResponseReadPerformancePostDto readPerformancePost(Long postId) {
        log.info("공연 모집 게시글 단건 조회 - postId={}", postId);

        Post post = postRepository
            .findByPostIdAndPostTypeAndIsDeletedFalse(postId, PERFORMANCE_POST_TYPE)
            .orElseThrow(() -> {
                log.error("조회 대상 게시글 없음 - postId={}", postId);
                return new ApiException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다.");
            });

        post.setViewCount(post.getViewCount() + 1);

        return ResponseReadPerformancePostDto.builder()
            .postId(post.getPostId())
            .postType(post.getPostType())
            .performanceId(post.getPerformance().getPerformanceId())
            .title(post.getTitle())
            .content(post.getContent())
            .viewCount(post.getViewCount())
            .createdAt(post.getCreatedAt())
            .build();
    }

    /**
     * [설명] 공연 모집 게시글을 수정합니다.
     *
     * @param postId 수정할 게시글 ID
     * @param dto 수정 요청 객체
     * @return 수정된 게시글 정보
     */
    public ResponseUpdatePerformancePostDto updatePerformancePost(
        Long postId,
        RequestUpdatePerformancePostDto dto
    ) {
        log.info("공연 모집 게시글 수정 요청 - postId={}", postId);

        Post post = postRepository
            .findByPostIdAndPostTypeAndIsDeletedFalse(postId, PERFORMANCE_POST_TYPE)
            .orElseThrow(() -> {
                log.error("수정 대상 게시글 없음 - postId={}", postId);
                return new ApiException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다.");
            });

        if (dto.getTitle() != null) {
            post.setTitle(dto.getTitle());
        }

        if (dto.getContent() != null) {
            post.setContent(dto.getContent());
        }

        if (dto.getPerformanceId() != null) {
            Performance performance = performanceRepository
                .findById(dto.getPerformanceId())
                .orElseThrow(() -> {
                    log.error("공연 정보 없음 - performanceId={}", dto.getPerformanceId());
                    return new ApiException(ErrorCode.NOT_FOUND, "공연 정보를 찾을 수 없습니다.");
                });
            post.setPerformance(performance);
        }

        log.info("공연 모집 게시글 수정 완료 - postId={}", postId);

        return ResponseUpdatePerformancePostDto.builder()
            .postId(post.getPostId())
            .postType(post.getPostType())
            .title(post.getTitle())
            .content(post.getContent())
            .performanceId(post.getPerformance().getPerformanceId())
            .updatedAt(post.getUpdatedAt())
            .build();
    }

    /**
     * [설명] 공연 모집 게시글 목록을 페이징 조회합니다.
     * 삭제되지 않은 게시글만 조회 대상입니다.
     *
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    @Transactional(readOnly = true)
    public Page<ResponseListPerformancePostDto> listPerformancePosts(Pageable pageable) {
        log.info(
            "공연 모집 게시글 목록 조회 - page={}, size={}",
            pageable.getPageNumber(),
            pageable.getPageSize()
        );

        return postRepository
            .findByPostTypeAndIsDeletedFalse(PERFORMANCE_POST_TYPE, pageable)
            .map(post -> ResponseListPerformancePostDto.builder()
                .postId(post.getPostId())
                .postType(post.getPostType())
                .performanceId(post.getPerformance().getPerformanceId())
                .title(post.getTitle())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .build());
    }
}
