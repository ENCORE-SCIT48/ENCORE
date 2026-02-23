package com.encore.encore.domain.community.service;

import com.encore.encore.domain.community.dto.PerformerPostDto.*;
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
public class PerformerPostService {

    private static final String PERFORMER_POST_TYPE = "PERFORMER_RECRUIT";

    private final PostRepository postRepository;
    private final PerformanceRepository performanceRepository;

    /**
     * [설명] 공연자 모집 게시글을 등록합니다.
     *
     * @param dto 게시글 등록 요청 객체
     * @return 등록된 게시글 정보
     */
    public ResponseCreatePerformerPostDto createPerformerPost(
            RequestCreatePerformerPostDto dto) {

        log.info("공연자 모집 게시글 등록 시작");

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
                .postType(PERFORMER_POST_TYPE)
                .title(dto.getTitle())
                .content(dto.getContent())
                .viewCount(0)
                .build();

        Post savedPost = postRepository.save(post);

        log.info("공연자 모집 게시글 등록 완료 - postId={}", savedPost.getPostId());

        return ResponseCreatePerformerPostDto.builder()
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
    public ResponseDeletePerformerPostDto deletePerformerPost(Long postId) {

        log.info("공연자 모집 게시글 삭제 요청 - postId={}", postId);

        Post post = postRepository
                .findByPostIdAndPostTypeAndIsDeletedFalse(postId, PERFORMER_POST_TYPE)
                .orElseThrow(() -> {
                    log.error("삭제 대상 게시글 없음 - postId={}", postId);
                    return new ApiException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다.");
                });

        post.delete();

        log.info("공연자 모집 게시글 삭제 완료 - postId={}", postId);

        return ResponseDeletePerformerPostDto.builder()
                .postId(postId)
                .deleted(true)
                .build();
    }

    /**
     * [설명] 공연자 모집 게시글 단건 상세 정보를 조회합니다.
     * 조회 시 조회수가 증가합니다.
     *
     * @param postId 게시글 ID
     * @return 게시글 상세 정보
     */
    public ResponseReadPerformerPostDto readPerformerPost(Long postId) {

        log.info("공연자 모집 게시글 단건 조회 - postId={}", postId);

        Post post = postRepository
                .findByPostIdAndPostTypeAndIsDeletedFalse(postId, PERFORMER_POST_TYPE)
                .orElseThrow(() -> {
                    log.error("조회 대상 게시글 없음 - postId={}", postId);
                    return new ApiException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다.");
                });

        post.setViewCount(post.getViewCount() + 1);

        return ResponseReadPerformerPostDto.builder()
                .postId(post.getPostId())
                .postType(post.getPostType())
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .build();
    }

    /**
     * [설명] 공연자 모집 게시글을 수정합니다.
     *
     * @param postId 수정할 게시글 ID
     * @param dto    수정 요청 객체
     * @return 수정된 게시글 정보
     */
    public ResponseUpdatePerformerPostDto updatePerformerPost(
            Long postId,
            RequestUpdatePerformerPostDto dto) {

        log.info("공연자 모집 게시글 수정 요청 - postId={}", postId);

        Post post = postRepository
                .findByPostIdAndPostTypeAndIsDeletedFalse(postId, PERFORMER_POST_TYPE)
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

        log.info("공연자 모집 게시글 수정 완료 - postId={}", postId);

        return ResponseUpdatePerformerPostDto.builder()
                .postId(post.getPostId())
                .postType(post.getPostType())
                .title(post.getTitle())
                .content(post.getContent())
                .performanceId(
                        post.getPerformance() != null
                                ? post.getPerformance().getPerformanceId()
                                : null)
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    /**
     * [설명] 공연자 모집 게시글 목록을 페이징 조회합니다.
     * 검색어가 존재하면 제목 검색을 수행합니다.
     *
     * @param keyword  검색어
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    @Transactional(readOnly = true)
    public Page<ResponseListPerformerPostDto> listPerformerPosts(
            String keyword,
            Pageable pageable) {

        log.info(
                "공연자 모집 게시글 목록 조회 - keyword={}, page={}, size={}",
                keyword,
                pageable.getPageNumber(),
                pageable.getPageSize());

        Page<Post> page;

        if (keyword == null || keyword.isBlank()) {
            page = postRepository
                    .findByPostTypeAndIsDeletedFalse(PERFORMER_POST_TYPE, pageable);
        } else {
            page = postRepository
                    .findByPostTypeAndTitleContainingIgnoreCaseAndIsDeletedFalse(
                            PERFORMER_POST_TYPE,
                            keyword,
                            pageable);
        }

        return page.map(post -> ResponseListPerformerPostDto.builder()
                .postId(post.getPostId())
                .postType(post.getPostType())
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .build());
    }
}
