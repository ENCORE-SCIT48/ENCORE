package com.encore.encore.domain.community.controller;

import com.encore.encore.domain.community.service.PostInteractionService;
import com.encore.encore.global.common.CommonResponse;
import com.encore.encore.global.config.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostInteractionController {

    private final PostInteractionService postInteractionService;

    /**
     * [설명] 게시글에 신청을 처리합니다.
     *
     * @param postId      게시글 ID
     * @param userDetails 로그인 사용자 정보
     * @return 신청 처리 결과
     */
    @PostMapping("/{postId}/apply")
    public CommonResponse<Void> apply(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("POST /api/posts/{}/apply - 신청 요청", postId);

        postInteractionService.applyToPost(postId, userDetails);

        return CommonResponse.ok(null, "신청이 완료되었습니다.");
    }

    /**
     * [설명] 신청을 승인 처리합니다.
     *
     * @param postId        게시글 ID
     * @param interactionId 신청 ID
     * @param userDetails   로그인 사용자 정보
     * @return 승인 처리 결과
     */
    @PutMapping("/{postId}/interactions/{interactionId}/approve")
    public CommonResponse<Void> approve(
            @PathVariable("postId") Long postId,
            @PathVariable("interactionId") Long interactionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("PATCH /api/posts/{}/interactions/{}/approve - 승인 요청",
                postId, interactionId);

        postInteractionService.approveInteraction(postId, interactionId, userDetails);

        return CommonResponse.ok(null, "승인이 완료되었습니다.");
    }

    /**
     * [설명] 신청을 거절 처리합니다.
     *
     * @param postId        게시글 ID
     * @param interactionId 신청 ID
     * @param userDetails   로그인 사용자 정보
     * @return 거절 처리 결과
     */
    @PutMapping("/{postId}/interactions/{interactionId}/reject")
    public CommonResponse<Void> reject(
            @PathVariable("postId") Long postId,
            @PathVariable("interactionId") Long interactionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("PATCH /api/posts/{}/interactions/{}/reject - 거절 요청",
                postId, interactionId);

        postInteractionService.rejectInteraction(postId, interactionId, userDetails);

        return CommonResponse.ok(null, "거절이 완료되었습니다.");
    }
}