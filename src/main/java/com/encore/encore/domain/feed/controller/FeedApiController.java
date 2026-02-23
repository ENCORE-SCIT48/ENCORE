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
        @RequestParam(required = false) Long userId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 개발용 우선순위: ?userId=8 이 있으면 그걸로 조회
        if (userId != null) {
            return feedService.getFeed(userId);
        }

        // 로그인 연동 전이면 기본값
        Long loginUserId = 8L;
        return feedService.getFeed(loginUserId);
    }
}
