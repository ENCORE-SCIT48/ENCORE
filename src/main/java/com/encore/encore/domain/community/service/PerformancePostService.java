package com.encore.encore.domain.community.service;

import com.encore.encore.domain.community.dto.PerformancePostDto.*;
import com.encore.encore.domain.community.entity.Post;
import com.encore.encore.domain.community.repository.PostRepository;
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

    private final PostRepository postRepository;
    private final PerformanceRepository performanceRepository;
    private final HostProfileRepository hostProfileRepository;
    private final PerformerProfileRepository performerProfileRepository;

    /**
     * [설명] 로그인 사용자의 PerformerProfile을 조회합니다.
     * 존재하지 않는 경우 테스트용 PerformerProfile을 생성 후 반환합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @return 조회되거나 새로 생성된 PerformerProfile
     */
    private PerformerProfile getOrCreatePerformerProfile(CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getUserId();
        log.info("PerformerProfile 조회 시작 - userId={}", userId);

        return performerProfileRepository.findByUser_UserId(userId)
                .orElseGet(() -> {

                    log.info("PerformerProfile 없음 → 테스트용 생성");

                    PerformerProfile newProfile = PerformerProfile.builder()
                            .user(userDetails.getUser())
                            .stageName(userDetails.getUser().getNickname())
                            .isInitialized(false)
                            .build();

                    PerformerProfile saved = performerProfileRepository.save(newProfile);

                    log.info("PerformerProfile 생성 완료 - performerId={}", saved.getPerformerId());

                    return saved;
                });
    }

    /**
     * [설명] 로그인 사용자의 HostProfile을 조회합니다.
     * 존재하지 않는 경우 테스트용 HostProfile을 생성 후 반환합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @return 조회되거나 새로 생성된 HostProfile
     */
    private HostProfile getOrCreateHostProfile(CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getUserId();
        log.info("HostProfile 조회 시작 - userId={}", userId);

        return hostProfileRepository.findByUser_UserId(userId)
                .orElseGet(() -> {

                    log.info("HostProfile 없음 → 테스트용 생성");

                    HostProfile newProfile = HostProfile.builder()
                            .user(userDetails.getUser())
                            .isInitialized(false)
                            .build();

                    HostProfile saved = hostProfileRepository.save(newProfile);

                    log.info("HostProfile 생성 완료 - hostId={}", saved.getHostId());

                    return saved;
                });
    }

    /**
     * [설명] 로그인 사용자의 활성 작성자 ID를 반환합니다.
     * PerformerProfile이 존재하면 performerId를 반환하고,
     * 없으면 HostProfile을 조회하여 hostId를 반환합니다.
     * 두 프로필이 모두 없으면 PerformerProfile을 생성 후 반환합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @return 활성 작성자 ID (비로그인 시 null)
     */
    public Long getActiveAuthorId(CustomUserDetails userDetails) {

        if (userDetails == null) {
            return null;
        }

        Long userId = userDetails.getUser().getUserId();

        PerformerProfile performer = performerProfileRepository
                .findByUser_UserId(userId)
                .orElse(null);

        if (performer != null) {
            return performer.getPerformerId();
        }

        HostProfile host = hostProfileRepository
                .findByUser_UserId(userId)
                .orElse(null);

        if (host != null) {
            return host.getHostId();
        }

        PerformerProfile newProfile = getOrCreatePerformerProfile(userDetails);
        return newProfile.getPerformerId();
    }

    /**
     * [설명] 공연 모집 게시글을 등록합니다.
     *
     * @param dto         게시글 등록 요청 객체
     * @param userDetails 로그인 사용자 정보
     * @return 등록된 게시글 정보
     */
    public ResponseCreatePerformancePostDto createPerformancePost(
            RequestCreatePerformancePostDto dto,
            CustomUserDetails userDetails) {

        log.info("공연 모집 게시글 등록 시작");

        if (userDetails == null) {
            throw new ApiException(ErrorCode.FORBIDDEN, "로그인이 필요합니다.");
        }

        getOrCreatePerformerProfile(userDetails);

        Long userId = userDetails.getUser().getUserId();

        HostProfile host = hostProfileRepository
                .findByUser_UserId(userId)
                .orElse(null);

        PerformerProfile performer = performerProfileRepository
                .findByUser_UserId(userId)
                .orElse(null);

        Performance performance = null;

        if (dto.getPerformanceId() != null) {
            performance = performanceRepository.findById(dto.getPerformanceId())
                    .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "공연 정보를 찾을 수 없습니다."));
        }

        Post post = Post.builder()
                .performance(performance)
                .postType(PERFORMANCE_POST_TYPE)
                .title(dto.getTitle())
                .content(dto.getContent())
                .viewCount(0)
                .hostAuthor(host)
                .performerAuthor(performer)
                .build();

        Post savedPost = postRepository.save(post);

        log.info("게시글 등록 완료 - postId={}", savedPost.getPostId());

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

        if (userDetails == null) {
            throw new ApiException(ErrorCode.FORBIDDEN, "로그인이 필요합니다.");
        }

        Long userId = userDetails.getUser().getUserId();

        Post post = postRepository
                .findByPostIdAndPostTypeAndIsDeletedFalse(postId, PERFORMANCE_POST_TYPE)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        boolean isHostOwner = post.getHostAuthor() != null &&
                post.getHostAuthor().getUser().getUserId().equals(userId);

        boolean isPerformerOwner = post.getPerformerAuthor() != null &&
                post.getPerformerAuthor().getUser().getUserId().equals(userId);

        if (!isHostOwner && !isPerformerOwner) {
            throw new ApiException(ErrorCode.FORBIDDEN, "삭제 권한이 없습니다.");
        }

        post.delete();

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

        if (userDetails == null) {
            throw new ApiException(ErrorCode.FORBIDDEN, "로그인이 필요합니다.");
        }

        Long userId = userDetails.getUser().getUserId();

        Post post = postRepository
                .findByPostIdAndPostTypeAndIsDeletedFalse(postId, PERFORMANCE_POST_TYPE)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        boolean isHostOwner = post.getHostAuthor() != null &&
                post.getHostAuthor().getUser().getUserId().equals(userId);

        boolean isPerformerOwner = post.getPerformerAuthor() != null &&
                post.getPerformerAuthor().getUser().getUserId().equals(userId);

        if (!isHostOwner && !isPerformerOwner) {
            throw new ApiException(ErrorCode.FORBIDDEN, "수정 권한이 없습니다.");
        }

        if (dto.getTitle() != null) {
            post.setTitle(dto.getTitle());
        }

        if (dto.getContent() != null) {
            post.setContent(dto.getContent());
        }

        return ResponseUpdatePerformancePostDto.builder()
                .postId(post.getPostId())
                .postType(post.getPostType())
                .title(post.getTitle())
                .content(post.getContent())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    /**
     * [설명] 공연 모집 게시글 단건 상세 정보를 조회합니다.
     *
     * @param postId 게시글 ID
     * @return 게시글 상세 정보
     */
    public ResponseReadPerformancePostDto readPerformancePost(Long postId) {

        Post post = postRepository
                .findByPostIdAndPostTypeAndIsDeletedFalse(postId, PERFORMANCE_POST_TYPE)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        post.setViewCount(post.getViewCount() + 1);

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

        return page.map(post -> ResponseListPerformancePostDto.builder()
                .postId(post.getPostId())
                .postType(post.getPostType())
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .build());
    }
}