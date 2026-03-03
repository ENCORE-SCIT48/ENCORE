package com.encore.encore.domain.community.service;

import com.encore.encore.domain.community.dto.PerformancePostDto.*;
import com.encore.encore.domain.community.entity.Post;
import com.encore.encore.domain.community.repository.PostRepository;
import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.member.entity.HostProfile;
import com.encore.encore.domain.member.entity.PerformerProfile;
import com.encore.encore.domain.member.repository.HostProfileRepository;
import com.encore.encore.domain.member.repository.PerformerProfileRepository;
import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.domain.performance.repository.PerformanceRepository;
import com.encore.encore.global.config.CustomUserDetails;
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

    private final PostInteractionService postInteractionService;

    private final PostRepository postRepository;
    private final PerformanceRepository performanceRepository;
    private final HostProfileRepository hostProfileRepository;
    private final PerformerProfileRepository performerProfileRepository;

    /**
     * [설명] 로그인 사용자의 현재 활성 모드에 따른 작성자 ID를 반환합니다.
     * - ROLE_PERFORMER: PerformerProfile의 performerId 반환
     * - ROLE_HOST: HostProfile의 hostId 반환
     * - ROLE_USER: 작성자 개념이 없으므로 null 반환
     *
     * @param userDetails 로그인 사용자 정보
     * @return 활성 작성자 ID (ROLE_USER 또는 비로그인 시 null)
     */
    public Long getActiveAuthorId(CustomUserDetails userDetails) {

        if (userDetails == null) {
            log.info("[ActiveAuthor] 비로그인 사용자 - authorId 반환 불가");
            return null;
        }

        ActiveMode activeMode = userDetails.getActiveMode();
        Long userId = userDetails.getUser().getUserId();

        log.info("[ActiveAuthor] 활성 모드 확인 - userId={}, mode={}", userId, activeMode);

        return switch (activeMode) {

            case ROLE_PERFORMER -> performerProfileRepository
                    .findByUser_UserId(userId)
                    .map(profile -> {
                        log.info("[ActiveAuthor] PerformerProfile 사용 - performerId={}", profile.getPerformerId());
                        return profile.getPerformerId();
                    })
                    .orElseThrow(() -> {
                        log.error("[ActiveAuthor] PerformerProfile 없음 - userId={}", userId);
                        return new IllegalStateException("PerformerProfile이 존재하지 않습니다.");
                    });

            case ROLE_HOST -> hostProfileRepository
                    .findByUser_UserId(userId)
                    .map(profile -> {
                        log.info("[ActiveAuthor] HostProfile 사용 - hostId={}", profile.getHostId());
                        return profile.getHostId();
                    })
                    .orElseThrow(() -> {
                        log.error("[ActiveAuthor] HostProfile 없음 - userId={}", userId);
                        return new IllegalStateException("HostProfile이 존재하지 않습니다.");
                    });

            case ROLE_USER -> {
                log.info("[ActiveAuthor] ROLE_USER - 작성자 ID 없음");
                yield null;
            }
        };
    }

    /**
     * [설명] 게시글의 작성 권한을 현재 활성 모드 기준으로 검증합니다.
     *
     * @param post        대상 게시글
     * @param userDetails 로그인 사용자 정보
     */
    private void validateOwnership(Post post, CustomUserDetails userDetails) {

        if (userDetails == null) {
            log.warn("[Ownership] 비로그인 사용자 접근");
            throw new ApiException(ErrorCode.FORBIDDEN, "로그인이 필요합니다.");
        }

        ActiveMode activeMode = userDetails.getActiveMode();
        Long authorId = getActiveAuthorId(userDetails);

        log.info("[Ownership] 권한 검증 시작 - postId={}, mode={}, authorId={}",
                post.getPostId(), activeMode, authorId);

        boolean isOwner = switch (activeMode) {

            case ROLE_PERFORMER ->
                post.getPerformerAuthor() != null &&
                        post.getPerformerAuthor().getPerformerId().equals(authorId);

            case ROLE_HOST ->
                post.getHostAuthor() != null &&
                        post.getHostAuthor().getHostId().equals(authorId);

            case ROLE_USER -> false;
        };

        if (!isOwner) {
            log.warn("[Ownership] 권한 없음 - postId={}, mode={}", post.getPostId(), activeMode);
            throw new ApiException(ErrorCode.FORBIDDEN, "권한이 없습니다.");
        }

        log.info("[Ownership] 권한 검증 완료 - postId={}", post.getPostId());
    }

    /**
     * [설명] 공연 모집 게시글을 등록합니다.
     * 활성 모드에 따라 작성자를 설정합니다.
     *
     * @param dto         게시글 등록 요청 객체
     * @param userDetails 로그인 사용자 정보
     * @return 등록된 게시글 정보
     */
    public ResponseCreatePerformancePostDto createPerformancePost(
            RequestCreatePerformancePostDto dto,
            CustomUserDetails userDetails) {

        if (userDetails == null) {
            log.warn("[CreatePerformancePost] 비로그인 사용자");
            throw new ApiException(ErrorCode.FORBIDDEN, "로그인이 필요합니다.");
        }

        ActiveMode activeMode = userDetails.getActiveMode();
        Long userId = userDetails.getUser().getUserId();

        log.info("[CreatePerformancePost] 시작 - userId={}, mode={}", userId, activeMode);

        HostProfile host = null;
        PerformerProfile performer = null;

        switch (activeMode) {

            case ROLE_PERFORMER -> performer = performerProfileRepository
                    .findByUser_UserId(userId)
                    .orElseThrow(() -> {
                        log.error("[CreatePerformancePost] PerformerProfile 없음 - userId={}", userId);
                        return new ApiException(ErrorCode.NOT_FOUND, "PerformerProfile이 존재하지 않습니다.");
                    });

            case ROLE_HOST -> host = hostProfileRepository
                    .findByUser_UserId(userId)
                    .orElseThrow(() -> {
                        log.error("[CreatePerformancePost] HostProfile 없음 - userId={}", userId);
                        return new ApiException(ErrorCode.NOT_FOUND, "HostProfile이 존재하지 않습니다.");
                    });

            case ROLE_USER -> {
                log.warn("[CreatePerformancePost] 관람객 작성 시도 - userId={}", userId);
                throw new ApiException(ErrorCode.FORBIDDEN, "관람객은 게시글을 작성할 수 없습니다.");
            }
        }

        log.info("[CreatePerformancePost] 작성자 설정 완료 - mode={}, hostId={}, performerId={}",
                activeMode,
                host != null ? host.getHostId() : null,
                performer != null ? performer.getPerformerId() : null);

        if (dto.getCapacity() == null || dto.getCapacity() <= 0) {
            log.warn("[CreatePerformancePost] 잘못된 정원 값 - userId={}", userId);
            throw new ApiException(ErrorCode.INVALID_REQUEST, "정원은 1명 이상이어야 합니다.");
        }

        Performance performance = null;
        if (dto.getPerformanceId() != null) {
            performance = performanceRepository.findById(dto.getPerformanceId())
                    .orElseThrow(() -> {
                        log.error("[CreatePerformancePost] 공연 정보 없음 - performanceId={}", dto.getPerformanceId());
                        return new ApiException(ErrorCode.NOT_FOUND, "공연 정보를 찾을 수 없습니다.");
                    });
        }

        Post post = Post.builder()
                .performance(performance)
                .postType(PERFORMANCE_POST_TYPE)
                .title(dto.getTitle())
                .content(dto.getContent())
                .viewCount(0)
                .capacity(dto.getCapacity())
                .hostAuthor(host)
                .performerAuthor(performer)
                .build();

        Post savedPost = postRepository.save(post);

        log.info("[CreatePerformancePost] 완료 - postId={}", savedPost.getPostId());

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
     * @param postId      게시글 ID
     * @param userDetails 로그인 사용자 정보
     * @return 삭제 결과
     */
    public ResponseDeletePerformancePostDto deletePerformancePost(
            Long postId,
            CustomUserDetails userDetails) {

        log.info("[DeletePerformancePost] 삭제 요청 - postId={}", postId);

        Post post = postRepository
                .findByPostIdAndPostTypeAndIsDeletedFalse(postId, PERFORMANCE_POST_TYPE)
                .orElseThrow(() -> {
                    log.error("[DeletePerformancePost] 게시글 없음 - postId={}", postId);
                    return new ApiException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다.");
                });

        validateOwnership(post, userDetails);

        post.delete();

        log.info("[DeletePerformancePost] 삭제 완료 - postId={}", postId);

        return ResponseDeletePerformancePostDto.builder()
                .postId(postId)
                .deleted(true)
                .build();
    }

    /**
     * [설명] 공연 모집 게시글을 수정합니다.
     *
     * @param postId      게시글 ID
     * @param dto         수정 요청 객체
     * @param userDetails 로그인 사용자 정보
     * @return 수정된 게시글 정보
     */
    public ResponseUpdatePerformancePostDto updatePerformancePost(
            Long postId,
            RequestUpdatePerformancePostDto dto,
            CustomUserDetails userDetails) {

        log.info("[UpdatePerformancePost] 수정 요청 - postId={}", postId);

        Post post = postRepository
                .findByPostIdAndPostTypeAndIsDeletedFalse(postId, PERFORMANCE_POST_TYPE)
                .orElseThrow(() -> {
                    log.error("[UpdatePerformancePost] 게시글 없음 - postId={}", postId);
                    return new ApiException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다.");
                });

        validateOwnership(post, userDetails);

        if (dto.getTitle() != null) {
            post.setTitle(dto.getTitle());
        }

        if (dto.getContent() != null) {
            post.setContent(dto.getContent());
        }

        if (dto.getCapacity() != null) {
            if (dto.getCapacity() <= 0) {
                log.warn("[UpdatePerformancePost] 잘못된 정원 값 - postId={}", postId);
                throw new ApiException(ErrorCode.INVALID_REQUEST, "정원은 1명 이상이어야 합니다.");
            }
            post.setCapacity(dto.getCapacity());
        }

        log.info("[UpdatePerformancePost] 수정 완료 - postId={}", postId);

        return ResponseUpdatePerformancePostDto.builder()
                .postId(post.getPostId())
                .postType(post.getPostType())
                .title(post.getTitle())
                .content(post.getContent())
                .capacity(post.getCapacity())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    /**
     * [설명] 공연 모집 게시글 단건 상세 정보를 조회합니다.
     *
     * - increaseView가 true인 경우에만 조회수를 증가시킵니다.
     * - 승인(APPROVED) 상태의 신청 인원 수를 함께 반환합니다.
     *
     * @param postId       게시글 ID
     * @param increaseView 조회수 증가 여부
     * @return 게시글 상세 정보
     */
    public ResponseReadPerformancePostDto readPerformancePost(
            Long postId,
            boolean increaseView) {

        log.info("[readPerformancePost] 상세 조회 시작 - postId={}, increaseView={}", postId, increaseView);

        Post post = postRepository
                .findByPostIdAndPostTypeAndIsDeletedFalse(postId, PERFORMANCE_POST_TYPE)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        // 조회수 증가 (상세 페이지에서만 증가)
        if (increaseView) {
            post.setViewCount(post.getViewCount() + 1);
        }

        int approvedCount = postInteractionService.getApprovedCount(post);

        return ResponseReadPerformancePostDto.builder()
                .postId(post.getPostId())
                .postType(post.getPostType())
                .performanceId(
                        post.getPerformance() != null
                                ? post.getPerformance().getPerformanceId()
                                : null)
                .hostId(
                        post.getHostAuthor() != null
                                ? post.getHostAuthor().getHostId()
                                : null)
                .performerId(
                        post.getPerformerAuthor() != null
                                ? post.getPerformerAuthor().getPerformerId()
                                : null)
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(post.getViewCount())
                .capacity(post.getCapacity())
                .approvedCount(approvedCount)
                .createdAt(post.getCreatedAt())
                .build();
    }

    /**
     * [설명] 공연 모집 게시글 목록을 페이징 조회합니다.
     *
     * @param keyword  검색어
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    @Transactional(readOnly = true)
    public Page<ResponseListPerformancePostDto> listPerformancePosts(
            String keyword,
            Pageable pageable) {

        Page<Post> page;

        if (keyword == null || keyword.isBlank()) {
            page = postRepository
                    .findByPostTypeAndIsDeletedFalse(PERFORMANCE_POST_TYPE, pageable);
        } else {
            page = postRepository
                    .findByPostTypeAndTitleContainingIgnoreCaseAndIsDeletedFalse(
                            PERFORMANCE_POST_TYPE,
                            keyword,
                            pageable);
        }

        return page.map(post -> {

            int approvedCount = postInteractionService.getApprovedCount(post);

            return ResponseListPerformancePostDto.builder()
                    .postId(post.getPostId())
                    .postType(post.getPostType())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .viewCount(post.getViewCount())
                    .capacity(post.getCapacity())
                    .approvedCount(approvedCount)
                    .createdAt(post.getCreatedAt())
                    .build();
        });
    }
}