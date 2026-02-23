package com.encore.encore.domain.community.service;

import com.encore.encore.domain.community.dto.PerformanceManageDto;
import com.encore.encore.domain.community.dto.PerformerPostDto.ResponseListPerformerPostDto;
import com.encore.encore.domain.community.entity.Post;
import com.encore.encore.domain.community.entity.PostInteraction;
import com.encore.encore.domain.community.repository.PostInteractionRepository;
import com.encore.encore.domain.community.repository.PostRepository;
import com.encore.encore.global.config.CustomUserDetails;
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
@Transactional(readOnly = true)
public class PerformerMypageService {

    private static final String PERFORMER_POST_TYPE = "PERFORMER_RECRUIT";
    private static final String PERFORMANCE_POST_TYPE = "PERFORMANCE_RECRUIT";
    private static final String APPLY_TYPE = "APPLY";

    private final PerformerPostService performerPostService;
    private final PostInteractionService postInteractionService;
    private final PostRepository postRepository;
    private final PostInteractionRepository postInteractionRepository;

    /**
     * [설명] 로그인 공연자가 작성한 공연자 모집글 목록을 조회합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @param pageable    페이징 정보
     * @return 공연자 모집글 페이지
     */
    public Page<ResponseListPerformerPostDto> findMyPerformerPosts(
            CustomUserDetails userDetails,
            Pageable pageable) {

        Long performerId = performerPostService.getActivePerformerId(userDetails);

        log.info("[PerformerMypageService] 내가 작성한 공연자 모집글 조회 - performerId={}", performerId);

        Page<Post> page = postRepository
                .findByPerformerAuthor_PerformerIdAndPostTypeAndIsDeletedFalse(
                        performerId,
                        PERFORMER_POST_TYPE,
                        pageable);

        return page.map(post -> {

            int approvedCount = postInteractionService.getApprovedCount(post);

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

    /**
     * [설명] 로그인 공연자가 신청한 공연자 모집글 목록을 조회합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @return 신청한 공연자 모집글 목록
     */
    public List<ResponseListPerformerPostDto> findAppliedPerformerPosts(
            CustomUserDetails userDetails) {

        Long performerId = performerPostService.getActivePerformerId(userDetails);

        List<PostInteraction> interactions = postInteractionRepository
                .findByApplicantPerformer_PerformerIdAndInteractionTypeAndPost_PostTypeAndIsDeletedFalse(
                        performerId,
                        APPLY_TYPE,
                        PERFORMER_POST_TYPE);

        return interactions.stream()
                .map(interaction -> {

                    Post post = interaction.getPost();

                    int approvedCount = postInteractionService.getApprovedCount(post);

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
                })
                .toList();
    }

    /**
     * [설명] 로그인 공연자가 작성한 공연 모집글 목록을 조회합니다.
     *
     * - performerAuthor 기준으로 조회합니다.
     * - postType이 PERFORMANCE_RECRUIT인 게시글만 조회합니다.
     * - 논리 삭제되지 않은 게시글만 조회합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @param pageable    페이징 정보
     * @return 공연 모집글 페이지
     */
    public Page<ResponseListPerformerPostDto> findMyPerformancePosts(
            CustomUserDetails userDetails,
            Pageable pageable) {

        Long performerId = performerPostService.getActivePerformerId(userDetails);

        log.info("[PerformerMypageService] 내가 작성한 공연 모집글 조회 - performerId={}", performerId);

        Page<Post> page = postRepository
                .findByPerformerAuthor_PerformerIdAndPostTypeAndIsDeletedFalse(
                        performerId,
                        PERFORMANCE_POST_TYPE,
                        pageable);

        return page.map(post -> {

            int approvedCount = postInteractionService.getApprovedCount(post);

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

    /**
     * [설명] 로그인 공연자가 신청한 공연 모집글 목록을 조회합니다.
     *
     * - interactionType이 APPLY인 경우만 조회합니다.
     * - 연관된 Post의 postType이 PERFORMANCE_RECRUIT인 경우만 조회합니다.
     * - 논리 삭제되지 않은 데이터만 조회합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @return 신청한 공연 모집글 목록
     */
    public List<ResponseListPerformerPostDto> findAppliedPerformances(
            CustomUserDetails userDetails) {

        Long performerId = performerPostService.getActivePerformerId(userDetails);

        log.info("[PerformerMypageService] 내가 신청한 공연 조회 - performerId={}", performerId);

        List<PostInteraction> interactions = postInteractionRepository
                .findByApplicantPerformer_PerformerIdAndInteractionTypeAndPost_PostTypeAndIsDeletedFalse(
                        performerId,
                        APPLY_TYPE,
                        PERFORMANCE_POST_TYPE);

        return interactions.stream()
                .map(interaction -> {

                    Post post = interaction.getPost();

                    int approvedCount = postInteractionService.getApprovedCount(post);

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
                })
                .toList();
    }

    /**
     * [설명] 로그인 공연자가 작성한 공연 모집글과
     * 해당 게시글에 신청한 공연자 목록을 함께 조회합니다.
     *
     * - performerAuthor 기준으로 PERFORMANCE_RECRUIT 게시글을 조회합니다.
     * - 각 게시글별로 interactionType이 APPLY인 신청 목록을 조회합니다.
     * - 논리 삭제되지 않은 데이터만 조회합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @return 공연 모집글과 신청자 목록 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<PerformanceManageDto> findMyPerformancePostsWithApplicants(
            CustomUserDetails userDetails) {

        Long performerId = performerPostService.getActivePerformerId(userDetails);

        log.info("[PerformerMypageService] 공연 모집글 신청자 관리 조회 - performerId={}", performerId);

        List<Post> posts = postRepository
                .findByPerformerAuthor_PerformerIdAndPostTypeAndIsDeletedFalse(
                        performerId,
                        PERFORMANCE_POST_TYPE);

        return posts.stream()
                .map(post -> {

                    List<PostInteraction> applicants = postInteractionRepository
                            .findByPost_PostIdAndInteractionTypeAndIsDeletedFalse(
                                    post.getPostId(),
                                    APPLY_TYPE);

                    return new PerformanceManageDto(post, applicants);
                })
                .toList();
    }
}