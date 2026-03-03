package com.encore.encore.domain.community.service;

import com.encore.encore.domain.community.entity.Post;
import com.encore.encore.domain.community.entity.PostInteraction;
import com.encore.encore.domain.community.repository.PostInteractionRepository;
import com.encore.encore.domain.community.repository.PostRepository;
import com.encore.encore.domain.member.entity.PerformerProfile;
import com.encore.encore.domain.member.repository.PerformerProfileRepository;
import com.encore.encore.global.config.CustomUserDetails;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PostInteractionService {

    private static final String APPLY_TYPE = "APPLY";
    private static final String PENDING = "PENDING";
    private static final String APPROVED = "APPROVED";
    private static final String REJECTED = "REJECTED";

    private final PostRepository postRepository;
    private final PostInteractionRepository postInteractionRepository;
    private final PerformerProfileRepository performerProfileRepository;

    /**
     * [설명] 게시글에 신청을 처리합니다.
     * 신청자는 항상 Performer이며, 본인 게시글 또는 중복 신청은 허용되지 않습니다.
     *
     * @param postId      게시글 ID
     * @param userDetails 로그인 사용자 정보
     */
    public void applyToPost(Long postId, CustomUserDetails userDetails) {

        log.info("[applyToPost] 신청 요청 - postId={}", postId);

        if (userDetails == null) {
            log.info("[applyToPost] 비로그인 상태");
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        Long userId = userDetails.getUser().getUserId();
        log.info("[applyToPost] userId={}", userId);

        PerformerProfile performer = performerProfileRepository
                .findByUser_UserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.FORBIDDEN, "PerformerProfile이 필요합니다."));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        boolean isHostOwner = post.getHostAuthor() != null &&
                post.getHostAuthor().getUser().getUserId().equals(userId);

        boolean isPerformerOwner = post.getPerformerAuthor() != null &&
                post.getPerformerAuthor().getUser().getUserId().equals(userId);

        if (isHostOwner || isPerformerOwner) {
            log.info("[applyToPost] 본인 게시글 신청 시도");
            throw new ApiException(ErrorCode.FORBIDDEN, "본인 게시글에는 신청할 수 없습니다.");
        }

        boolean alreadyApplied = postInteractionRepository
                .existsByPostAndApplicantPerformer_PerformerIdAndInteractionTypeAndIsDeletedFalse(
                        post,
                        performer.getPerformerId(),
                        APPLY_TYPE);

        if (alreadyApplied) {
            log.info("[applyToPost] 중복 신청 - performerId={}", performer.getPerformerId());
            throw new ApiException(ErrorCode.CONFLICT, "이미 신청한 게시글입니다.");
        }

        // 모집 마감 체크
        if (isCapacityFull(post)) {
            log.info("[applyToPost] 모집 마감 상태 - postId={}", postId);
            throw new ApiException(ErrorCode.CONFLICT, "모집이 마감된 게시글입니다.");
        }

        PostInteraction interaction = PostInteraction.builder()
                .post(post)
                .applicantPerformer(performer)
                .interactionType(APPLY_TYPE)
                .status(PENDING)
                .build();

        postInteractionRepository.save(interaction);

        log.info("[applyToPost] 신청 완료 - postId={}, performerId={}",
                postId, performer.getPerformerId());
    }

    /**
     * [설명] 신청을 승인 처리합니다.
     * 게시글 작성자만 승인할 수 있으며, PENDING 상태일 때만 가능합니다.
     *
     * @param postId        게시글 ID
     * @param interactionId 신청 ID
     * @param userDetails   로그인 사용자 정보
     */
    public void approveInteraction(Long postId, Long interactionId, CustomUserDetails userDetails) {

        log.info("[approveInteraction] 승인 요청 - postId={}, interactionId={}", postId, interactionId);

        if (userDetails == null) {
            log.info("[approveInteraction] 비로그인 상태");
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        Long userId = userDetails.getUser().getUserId();

        PostInteraction interaction = postInteractionRepository.findById(interactionId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "신청 정보를 찾을 수 없습니다."));

        Post post = interaction.getPost();

        if (!post.getPostId().equals(postId)) {
            log.info("[approveInteraction] 게시글 불일치");
            throw new ApiException(ErrorCode.INVALID_REQUEST, "게시글 정보가 일치하지 않습니다.");
        }

        boolean isHostOwner = post.getHostAuthor() != null &&
                post.getHostAuthor().getUser().getUserId().equals(userId);

        boolean isPerformerOwner = post.getPerformerAuthor() != null &&
                post.getPerformerAuthor().getUser().getUserId().equals(userId);

        if (!isHostOwner && !isPerformerOwner) {
            log.info("[approveInteraction] 권한 없음 - userId={}", userId);
            throw new ApiException(ErrorCode.FORBIDDEN, "승인 권한이 없습니다.");
        }

        if (!PENDING.equals(interaction.getStatus())) {
            log.info("[approveInteraction] 이미 처리된 상태 - status={}", interaction.getStatus());
            throw new ApiException(ErrorCode.CONFLICT, "이미 처리된 신청입니다.");
        }

        // 정원 체크
        if (isCapacityFull(post)) {
            log.info("[approveInteraction] 모집 인원 마감 - postId={}", postId);
            throw new ApiException(ErrorCode.CONFLICT, "모집 인원이 마감되었습니다.");
        }

        interaction.setStatus(APPROVED);

        log.info("[approveInteraction] 승인 완료 - interactionId={}", interactionId);
    }

    /**
     * [설명] 신청을 거절 처리합니다.
     * 게시글 작성자만 거절할 수 있으며, PENDING 상태일 때만 가능합니다.
     *
     * @param postId        게시글 ID
     * @param interactionId 신청 ID
     * @param userDetails   로그인 사용자 정보
     */
    public void rejectInteraction(Long postId, Long interactionId, CustomUserDetails userDetails) {

        log.info("[rejectInteraction] 거절 요청 - postId={}, interactionId={}", postId, interactionId);

        if (userDetails == null) {
            log.info("[rejectInteraction] 비로그인 상태");
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        Long userId = userDetails.getUser().getUserId();

        PostInteraction interaction = postInteractionRepository.findById(interactionId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "신청 정보를 찾을 수 없습니다."));

        Post post = interaction.getPost();

        if (!post.getPostId().equals(postId)) {
            log.info("[rejectInteraction] 게시글 불일치");
            throw new ApiException(ErrorCode.INVALID_REQUEST, "게시글 정보가 일치하지 않습니다.");
        }

        boolean isHostOwner = post.getHostAuthor() != null &&
                post.getHostAuthor().getUser().getUserId().equals(userId);

        boolean isPerformerOwner = post.getPerformerAuthor() != null &&
                post.getPerformerAuthor().getUser().getUserId().equals(userId);

        if (!isHostOwner && !isPerformerOwner) {
            log.info("[rejectInteraction] 권한 없음 - userId={}", userId);
            throw new ApiException(ErrorCode.FORBIDDEN, "거절 권한이 없습니다.");
        }

        if (!PENDING.equals(interaction.getStatus())) {
            log.info("[rejectInteraction] 이미 처리된 상태 - status={}", interaction.getStatus());
            throw new ApiException(ErrorCode.CONFLICT, "이미 처리된 신청입니다.");
        }

        interaction.setStatus(REJECTED);

        log.info("[rejectInteraction] 거절 완료 - interactionId={}", interactionId);
    }

    /**
     * [설명] 로그인 사용자가 해당 게시글에 이미 신청했는지 여부를 반환합니다.
     *
     * @param postId      게시글 ID
     * @param userDetails 로그인 사용자 정보
     * @return 이미 신청했으면 true, 아니면 false
     */
    @Transactional(readOnly = true)
    public boolean isAlreadyApplied(Long postId, CustomUserDetails userDetails) {

        log.info("[isAlreadyApplied] 신청 여부 확인 - postId={}", postId);

        if (userDetails == null) {
            log.info("[isAlreadyApplied] 비로그인 상태");
            return false;
        }

        Long userId = userDetails.getUser().getUserId();

        PerformerProfile performer = performerProfileRepository
                .findByUser_UserId(userId)
                .orElse(null);

        if (performer == null) {
            log.info("[isAlreadyApplied] PerformerProfile 없음 - userId={}", userId);
            return false;
        }

        Post post = postRepository.findById(postId).orElse(null);

        if (post == null) {
            log.info("[isAlreadyApplied] 게시글 없음 - postId={}", postId);
            return false;
        }

        boolean result = postInteractionRepository
                .existsByPostAndApplicantPerformer_PerformerIdAndInteractionTypeAndIsDeletedFalse(
                        post,
                        performer.getPerformerId(),
                        APPLY_TYPE);

        log.info("[isAlreadyApplied] 결과 - performerId={}, alreadyApplied={}",
                performer.getPerformerId(), result);

        return result;
    }

    /**
     * [설명] 특정 게시글의 승인된 신청 인원 수를 반환합니다.
     *
     * @param post 게시글 엔티티
     * @return 승인 인원 수
     */
    @Transactional(readOnly = true)
    public int getApprovedCount(Post post) {

        long count = postInteractionRepository
                .countByPostAndInteractionTypeAndStatusAndIsDeletedFalse(
                        post,
                        APPLY_TYPE,
                        APPROVED);


        return (int) count;
    }

    /**
     * [설명] 특정 게시글이 모집 정원을 초과했는지 여부를 반환합니다.
     *
     * - Post에 설정된 capacity를 기준으로 판단합니다.
     * - 승인(APPROVED) 상태인 신청 수와 비교합니다.
     * - capacity가 null이거나 0 이하인 경우 정원 제한이 없는 것으로 간주합니다.
     *
     * @param post 게시글 엔티티
     * @return 정원 초과 또는 마감 상태이면 true
     */
    @Transactional(readOnly = true)
    public boolean isCapacityFull(Post post) {

        log.info("[isCapacityFull] 정원 확인 시작 - postId={}", post.getPostId());

        Integer capacity = post.getCapacity();

        if (capacity == null || capacity <= 0) {
            log.info("[isCapacityFull] 정원 미설정 - postId={}", post.getPostId());
            return false;
        }

        int approvedCount = getApprovedCount(post);

        boolean result = approvedCount >= capacity;

        log.info("[isCapacityFull] approvedCount={}, capacity={}, full={}",
                approvedCount, capacity, result);

        return result;
    }

    /**
     * [설명] 특정 게시글 ID 기준으로 승인된 신청 인원 수를 반환합니다.
     *
     * - 게시글 엔티티를 조회한 후 승인(APPROVED) 상태의 신청 수를 계산합니다.
     * - 상세 페이지 모집현황 표시용으로 사용됩니다.
     *
     * @param postId 게시글 ID
     * @return 승인 인원 수
     */
    @Transactional(readOnly = true)
    public int getApprovedCountByPostId(Long postId) {

        log.info("[getApprovedCountByPostId] 승인 인원 조회 시작 - postId={}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        long count = postInteractionRepository
                .countByPostAndInteractionTypeAndStatusAndIsDeletedFalse(
                        post,
                        APPLY_TYPE,
                        APPROVED);

        log.info("[getApprovedCountByPostId] 승인 인원 수={}", count);

        return (int) count;
    }
}