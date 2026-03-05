package com.encore.encore.domain.community.service;

import com.encore.encore.domain.community.dto.PerformerPostDto.*;
import com.encore.encore.domain.community.entity.Post;
import com.encore.encore.domain.community.repository.PostRepository;
import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.member.entity.PerformerProfile;
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
public class PerformerPostService {

    private static final String PERFORMER_POST_TYPE = "PERFORMER_RECRUIT";

    private final PostInteractionService postInteractionService;

    private final PostRepository postRepository;
    private final PerformanceRepository performanceRepository;
    private final PerformerProfileRepository performerProfileRepository;

    /**
     * [설명] 공연자 모집 게시글의 작성 권한을 검증합니다.
     *
     * @param post        대상 게시글
     * @param userDetails 로그인 사용자 정보
     */
    private void validateOwnership(Post post, CustomUserDetails userDetails) {

        if (userDetails == null) {
            log.warn("[Ownership] 비로그인 사용자");
            throw new ApiException(ErrorCode.FORBIDDEN, "로그인이 필요합니다.");
        }

        ActiveMode activeMode = userDetails.getActiveMode();
        Long userId = userDetails.getUser().getUserId();

        log.info("[Ownership] 시작 - postId={}, userId={}, mode={}",
                post.getPostId(), userId, activeMode);

        if (activeMode != ActiveMode.ROLE_PERFORMER) {
            log.warn("[Ownership] 공연자 모드 아님 - mode={}", activeMode);
            throw new ApiException(ErrorCode.FORBIDDEN, "공연자 모드에서만 가능합니다.");
        }

        PerformerProfile performer = performerProfileRepository
                .findByUser_UserId(userId)
                .orElseThrow(() -> {
                    log.error("[Ownership] PerformerProfile 없음 - userId={}", userId);
                    return new ApiException(ErrorCode.NOT_FOUND, "PerformerProfile이 존재하지 않습니다.");
                });

        if (post.getPerformerAuthor() == null ||
                !post.getPerformerAuthor().getPerformerId().equals(performer.getPerformerId())) {

            log.warn("[Ownership] 권한 없음 - postId={}, performerId={}",
                    post.getPostId(), performer.getPerformerId());
            throw new ApiException(ErrorCode.FORBIDDEN, "권한이 없습니다.");
        }

        log.info("[Ownership] 검증 완료 - postId={}", post.getPostId());
    }

    /**
     * [설명] 로그인 사용자의 활성 PerformerProfile ID를 반환합니다.
     * 활성 모드가 ROLE_PERFORMER일 때만 반환합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @return performerId (ROLE_PERFORMER 아닐 경우 null)
     */
    public Long getActivePerformerId(CustomUserDetails userDetails) {

        if (userDetails == null) {
            log.warn("[ActivePerformerId] 비로그인 사용자");
            return null;
        }

        ActiveMode activeMode = userDetails.getActiveMode();
        Long userId = userDetails.getUser().getUserId();

        log.info("[ActivePerformerId] 요청 - userId={}, mode={}", userId, activeMode);

        if (activeMode != ActiveMode.ROLE_PERFORMER) {
            log.info("[ActivePerformerId] 공연자 모드 아님 - null 반환");
            return null;
        }

        PerformerProfile performer = performerProfileRepository
                .findByUser_UserId(userId)
                .orElseThrow(() -> {
                    log.error("[ActivePerformerId] PerformerProfile 없음 - userId={}", userId);
                    return new ApiException(ErrorCode.NOT_FOUND, "PerformerProfile이 존재하지 않습니다.");
                });

        log.info("[ActivePerformerId] 반환 - performerId={}", performer.getPerformerId());

        return performer.getPerformerId();
    }

    /**
     * [설명] 공연자 모집 게시글을 등록합니다.
     * 활성 모드가 ROLE_PERFORMER일 때만 가능합니다.
     *
     * @param dto         게시글 등록 요청 객체
     * @param userDetails 로그인 사용자 정보
     * @return 등록된 게시글 정보
     */
    public ResponseCreatePerformerPostDto createPerformerPost(
            RequestCreatePerformerPostDto dto,
            CustomUserDetails userDetails) {

        if (userDetails == null) {
            log.warn("[CreatePerformerPost] 비로그인 사용자");
            throw new ApiException(ErrorCode.FORBIDDEN, "로그인이 필요합니다.");
        }

        ActiveMode activeMode = userDetails.getActiveMode();
        Long userId = userDetails.getUser().getUserId();

        log.info("[CreatePerformerPost] 시작 - userId={}, mode={}", userId, activeMode);

        if (activeMode != ActiveMode.ROLE_PERFORMER) {
            log.warn("[CreatePerformerPost] 공연자 모드 아님 - mode={}", activeMode);
            throw new ApiException(ErrorCode.FORBIDDEN, "공연자 모드에서만 작성 가능합니다.");
        }

        PerformerProfile performer = performerProfileRepository
                .findByUser_UserId(userId)
                .orElseThrow(() -> {
                    log.error("[CreatePerformerPost] PerformerProfile 없음 - userId={}", userId);
                    return new ApiException(ErrorCode.NOT_FOUND, "PerformerProfile이 존재하지 않습니다.");
                });

        if (dto.getCapacity() == null || dto.getCapacity() <= 0) {
            log.warn("[CreatePerformerPost] 잘못된 정원 값 - userId={}", userId);
            throw new ApiException(ErrorCode.INVALID_REQUEST, "정원은 1명 이상이어야 합니다.");
        }

        Performance performance = null;
        if (dto.getPerformanceId() != null) {
            performance = performanceRepository.findById(dto.getPerformanceId())
                    .orElseThrow(() -> {
                        log.error("[CreatePerformerPost] 공연 정보 없음 - performanceId={}", dto.getPerformanceId());
                        return new ApiException(ErrorCode.NOT_FOUND, "공연 정보를 찾을 수 없습니다.");
                    });
        }

        String categoryString = null;
        if (dto.getRecruitCategory() != null && !dto.getRecruitCategory().isEmpty()) {
            categoryString = String.join(",", dto.getRecruitCategory());
        }

        String partString = null;
        if (dto.getRecruitPart() != null && !dto.getRecruitPart().isEmpty()) {
            partString = String.join(",", dto.getRecruitPart());
        }

        Post post = Post.builder()
                .performance(performance)
                .postType(PERFORMER_POST_TYPE)
                .title(dto.getTitle())
                .content(dto.getContent())
                .viewCount(0)
                .capacity(dto.getCapacity())
                .performerAuthor(performer)
                .recruitCategory(categoryString)
                .recruitPart(partString)
                .recruitArea(dto.getRecruitArea())
                .build();

        Post savedPost = postRepository.save(post);

        log.info("[CreatePerformerPost] 완료 - postId={}", savedPost.getPostId());

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
     * @param postId      삭제할 게시글 ID
     * @param userDetails 로그인 사용자 정보
     * @return 삭제 결과
     */
    public ResponseDeletePerformerPostDto deletePerformerPost(
            Long postId,
            CustomUserDetails userDetails) {

        log.info("[DeletePerformerPost] 삭제 요청 - postId={}", postId);

        Post post = postRepository
                .findByPostIdAndPostTypeAndIsDeletedFalse(postId, PERFORMER_POST_TYPE)
                .orElseThrow(() -> {
                    log.error("[DeletePerformerPost] 게시글 없음 - postId={}", postId);
                    return new ApiException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다.");
                });

        validateOwnership(post, userDetails);

        post.delete();

        log.info("[DeletePerformerPost] 삭제 완료 - postId={}", postId);

        return ResponseDeletePerformerPostDto.builder()
                .postId(postId)
                .deleted(true)
                .build();
    }

    /**
     * [설명] 공연자 모집 게시글을 수정합니다.
     *
     * @param postId      수정할 게시글 ID
     * @param dto         수정 요청 객체
     * @param userDetails 로그인 사용자 정보
     * @return 수정된 게시글 정보
     */
    public ResponseUpdatePerformerPostDto updatePerformerPost(
            Long postId,
            RequestUpdatePerformerPostDto dto,
            CustomUserDetails userDetails) {

        log.info("[UpdatePerformerPost] 수정 요청 - postId={}", postId);

        Post post = postRepository
                .findByPostIdAndPostTypeAndIsDeletedFalse(postId, PERFORMER_POST_TYPE)
                .orElseThrow(() -> {
                    log.error("[UpdatePerformerPost] 게시글 없음 - postId={}", postId);
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
                log.warn("[UpdatePerformerPost] 잘못된 정원 값 - postId={}", postId);
                throw new ApiException(ErrorCode.INVALID_REQUEST, "정원은 1명 이상이어야 합니다.");
            }
            post.setCapacity(dto.getCapacity());
        }

        if (dto.getRecruitCategory() != null) {
            post.setRecruitCategory(String.join(",", dto.getRecruitCategory()));
        }

        if (dto.getRecruitPart() != null) {
            post.setRecruitPart(String.join(",", dto.getRecruitPart()));
        }

        if (dto.getRecruitArea() != null) {
            post.setRecruitArea(dto.getRecruitArea());
        }

        log.info("[UpdatePerformerPost] 수정 완료 - postId={}", postId);

        return ResponseUpdatePerformerPostDto.builder()
                .postId(post.getPostId())
                .postType(post.getPostType())
                .title(post.getTitle())
                .content(post.getContent())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    /**
     * [설명] 공연자 모집 게시글 단건 상세 정보를 조회합니다.
     *
     * - 게시글을 단건 조회합니다.
     * - increaseView가 true인 경우에만 조회수를 증가시킵니다.
     * - 승인(APPROVED) 상태 신청 인원 수를 함께 조회합니다.
     *
     * @param postId       게시글 ID
     * @param increaseView 조회수 증가 여부
     * @return 게시글 상세 정보
     */
    public ResponseReadPerformerPostDto readPerformerPost(
            Long postId,
            boolean increaseView) {

        log.info("[readPerformerPost] 상세 조회 시작 - postId={}, increaseView={}",
                postId, increaseView);

        Post post = postRepository
                .findByPostIdAndPostTypeAndIsDeletedFalse(postId, PERFORMER_POST_TYPE)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        // 조회수 증가 (상세 페이지에서만 증가)
        if (increaseView) {
            post.setViewCount(post.getViewCount() + 1);

            log.info("[readPerformerPost] 조회수 증가 - postId={}, viewCount={}",
                    postId, post.getViewCount());
        } else {
            log.info("[readPerformerPost] 조회수 증가 없음 - 수정/기타 조회");
        }

        int approvedCount = postInteractionService.getApprovedCount(post);

        log.info("[readPerformerPost] 승인 인원 조회 완료 - postId={}, approvedCount={}",
                postId, approvedCount);

        return ResponseReadPerformerPostDto.builder()
                .postId(post.getPostId())
                .postType(post.getPostType())
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(post.getViewCount())
                .capacity(post.getCapacity())
                .approvedCount(approvedCount)
                .createdAt(post.getCreatedAt())
                .performerId(post.getPerformerAuthor().getPerformerId())
                .recruitCategory(post.getRecruitCategory())
                .recruitPart(post.getRecruitPart())
                .recruitArea(post.getRecruitArea())
                .build();
    }

    /**
     * [설명] 공연자 모집 게시글 목록을 페이징 조회합니다.
     *
     * - postType이 PERFORMER_RECRUIT인 게시글만 조회합니다.
     * - 검색어(keyword)가 존재하면 제목 기준 검색을 수행합니다.
     * - 각 게시글의 승인(APPROVED) 상태 신청 인원 수를 함께 조회합니다.
     * - 정원(capacity)과 승인 인원(approvedCount)을 목록에 포함합니다.
     *
     * @param keyword  검색어
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    @Transactional(readOnly = true)
    public Page<ResponseListPerformerPostDto> listPerformerPosts(
            String keyword,
            Pageable pageable) {

        log.info("[listPerformerPosts] 목록 조회 시작 - keyword={}, page={}, size={}",
                keyword, pageable.getPageNumber(), pageable.getPageSize());

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

        log.info("[listPerformerPosts] 조회 완료 - totalElements={}", page.getTotalElements());

        return page.map(post -> {

            int approvedCount = postInteractionService.getApprovedCount(post);

            log.debug("[listPerformerPosts] postId={}, approvedCount={}, capacity={}",
                    post.getPostId(), approvedCount, post.getCapacity());

            return ResponseListPerformerPostDto.builder()
                    .postId(post.getPostId())
                    .postType(post.getPostType())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .capacity(post.getCapacity())
                    .approvedCount(approvedCount)
                    .viewCount(post.getViewCount())
                    .createdAt(post.getCreatedAt())
                    .build();
        });
    }
}
