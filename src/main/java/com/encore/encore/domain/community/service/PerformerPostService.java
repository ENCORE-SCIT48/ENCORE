package com.encore.encore.domain.community.service;

import com.encore.encore.domain.community.dto.PerformerPostDto.*;
import com.encore.encore.domain.community.entity.Post;
import com.encore.encore.domain.community.repository.PostRepository;
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
     * [설명] 로그인 사용자의 PerformerProfile을 조회합니다.
     * 해당 사용자의 PerformerProfile이 존재하지 않는 경우
     * 테스트용 PerformerProfile을 새로 생성하여 반환합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @return 조회되거나 새로 생성된 PerformerProfile
     */
    private PerformerProfile getOrCreatePerformerProfile(CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getUserId();
        log.info("PerformerProfile 조회 시작 - userId={}", userId);

        PerformerProfile performer = performerProfileRepository
                .findByUser_UserId(userId)
                .orElse(null);

        if (performer != null) {
            log.info("기존 PerformerProfile 사용 - performerId={}", performer.getPerformerId());
            return performer;
        }

        log.info("PerformerProfile 없음 → 테스트용 생성");

        PerformerProfile newProfile = PerformerProfile.builder()
                .user(userDetails.getUser())
                .stageName(userDetails.getUser().getNickname())
                .isInitialized(false)
                .build();

        PerformerProfile saved = performerProfileRepository.save(newProfile);

        log.info("PerformerProfile 생성 완료 - performerId={}", saved.getPerformerId());

        return saved;
    }

    /**
     * [설명] 로그인 사용자의 활성 PerformerProfile ID를 반환합니다.
     * 프로필이 없으면 생성 후 반환합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @return performerId (비로그인 시 null)
     */
    public Long getActivePerformerId(CustomUserDetails userDetails) {

        if (userDetails == null) {
            log.info("activePerformerId 요청 - 비로그인 상태");
            return null;
        }

        log.info("activePerformerId 조회 시작 - userId={}",
                userDetails.getUser().getUserId());

        PerformerProfile profile = getOrCreatePerformerProfile(userDetails);

        log.info("activePerformerId 반환 - performerId={}",
                profile.getPerformerId());

        return profile.getPerformerId();
    }

    /**
     * [설명] 공연자 모집 게시글을 등록합니다.
     *
     * - 로그인 사용자만 등록할 수 있습니다.
     * - 정원(capacity)은 1명 이상이어야 합니다.
     * - 공연 정보(performanceId)가 존재하면 연관 설정합니다.
     * - 초기 조회수는 0으로 설정합니다.
     *
     * @param dto         게시글 등록 요청 객체
     * @param userDetails 로그인 사용자 정보
     * @return 등록된 게시글 정보
     */
    public ResponseCreatePerformerPostDto createPerformerPost(
            RequestCreatePerformerPostDto dto,
            CustomUserDetails userDetails) {

        log.info("공연자 모집 게시글 등록 시작");

        if (userDetails == null) {
            log.info("비로그인 상태 - 등록 차단");
            throw new ApiException(ErrorCode.FORBIDDEN, "로그인이 필요합니다.");
        }

        PerformerProfile performer = getOrCreatePerformerProfile(userDetails);
        log.info("작성자 performerId={}", performer.getPerformerId());

        Performance performance = null;
        if (dto.getPerformanceId() != null) {
            log.info("Performance 조회 - performanceId={}", dto.getPerformanceId());

            performance = performanceRepository.findById(dto.getPerformanceId())
                    .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "공연 정보를 찾을 수 없습니다."));
        }

        if (dto.getCapacity() == null || dto.getCapacity() <= 0) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "정원은 1명 이상이어야 합니다.");
        }

        Post post = Post.builder()
                .performance(performance)
                .postType(PERFORMER_POST_TYPE)
                .title(dto.getTitle())
                .content(dto.getContent())
                .viewCount(0)
                .capacity(dto.getCapacity())
                .performerAuthor(performer)
                .build();

        Post savedPost = postRepository.save(post);

        log.info("게시글 등록 완료 - postId={}, performerId={}",
                savedPost.getPostId(), performer.getPerformerId());

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
     * @return 게시글 삭제 결과
     */
    public ResponseDeletePerformerPostDto deletePerformerPost(
            Long postId,
            CustomUserDetails userDetails) {

        log.info("게시글 삭제 요청 - postId={}", postId);

        if (userDetails == null) {
            log.info("비로그인 상태 - 삭제 차단");
            throw new ApiException(ErrorCode.FORBIDDEN, "로그인이 필요합니다.");
        }

        PerformerProfile performer = getOrCreatePerformerProfile(userDetails);

        Post post = postRepository
                .findByPostIdAndPostTypeAndIsDeletedFalse(postId, PERFORMER_POST_TYPE)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        log.info("삭제 권한 체크 - 작성자={}, 요청자={}",
                post.getPerformerAuthor().getPerformerId(),
                performer.getPerformerId());

        if (!post.getPerformerAuthor().getPerformerId()
                .equals(performer.getPerformerId())) {

            log.info("삭제 권한 없음");
            throw new ApiException(ErrorCode.FORBIDDEN, "삭제 권한이 없습니다.");
        }

        post.delete();

        log.info("게시글 삭제 완료 - postId={}", postId);

        return ResponseDeletePerformerPostDto.builder()
                .postId(postId)
                .deleted(true)
                .build();
    }

    /**
     * [설명] 공연자 모집 게시글을 수정합니다.
     *
     * - 로그인 사용자이면서 작성자 본인만 수정 가능합니다.
     * - 제목, 내용, 정원(capacity)을 수정할 수 있습니다.
     * - 정원은 1명 이상이어야 합니다.
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

        log.info("게시글 수정 요청 - postId={}", postId);

        if (userDetails == null) {
            log.info("비로그인 상태 - 수정 차단");
            throw new ApiException(ErrorCode.FORBIDDEN, "로그인이 필요합니다.");
        }

        PerformerProfile performer = getOrCreatePerformerProfile(userDetails);

        Post post = postRepository
                .findByPostIdAndPostTypeAndIsDeletedFalse(postId, PERFORMER_POST_TYPE)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        log.info("수정 권한 체크 - 작성자={}, 요청자={}",
                post.getPerformerAuthor().getPerformerId(),
                performer.getPerformerId());

        if (!post.getPerformerAuthor().getPerformerId()
                .equals(performer.getPerformerId())) {

            log.info("수정 권한 없음");
            throw new ApiException(ErrorCode.FORBIDDEN, "수정 권한이 없습니다.");
        }

        if (dto.getTitle() != null) {
            post.setTitle(dto.getTitle());
        }

        if (dto.getContent() != null) {
            post.setContent(dto.getContent());
        }

        if (dto.getCapacity() != null) {
            if (dto.getCapacity() <= 0) {
                throw new ApiException(ErrorCode.INVALID_REQUEST, "정원은 1명 이상이어야 합니다.");
            }
            post.setCapacity(dto.getCapacity());
        }

        log.info("게시글 수정 완료 - postId={}", postId);

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

            log.info("[listPerformerPosts] postId={}, approvedCount={}, capacity={}",
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
