package com.encore.encore.domain.feed.controller;

import com.encore.encore.domain.feed.dto.FeedResponseDto;
import com.encore.encore.domain.feed.service.FeedService;
import com.encore.encore.global.config.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feed")
public class FeedApiController {

    private final FeedService feedService;

    @GetMapping
    public FeedResponseDto getFeed(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 비로그인/세션 없음 → 게스트용 피드
        if (userDetails == null || userDetails.getUser() == null) {
            return feedService.getGuestFeed();
        }

        Long loginUserId = userDetails.getUser().getUserId();
        return feedService.getFeed(loginUserId);
    }
}
