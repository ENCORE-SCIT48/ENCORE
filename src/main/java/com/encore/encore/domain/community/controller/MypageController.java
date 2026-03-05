package com.encore.encore.domain.community.controller;

import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.member.repository.HostProfileRepository;
import com.encore.encore.domain.member.repository.PerformerProfileRepository;
import com.encore.encore.domain.user.service.RelationService;
import com.encore.encore.global.config.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MypageController {

    private final PerformerProfileRepository performerProfileRepository;
    private final HostProfileRepository hostProfileRepository;
    private final RelationService relationService;

    /**
     * [설명] 통합 마이페이지 화면을 조회합니다.
     * 로그인 사용자의 프로필 존재 여부에 따라 profileMode를 결정하여 화면에 전달합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @param model       화면 전달 객체
     * @return 마이페이지 화면 경로
     */
    @GetMapping("/mypage")
    public String mypage(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        Model model) {

        if (userDetails == null) {
            log.info("[MypageController] 비로그인 사용자의 마이페이지 접근");
            return "redirect:/auth/login";
        }

        Long userId = userDetails.getUser().getUserId();
        log.info("[MypageController] 마이페이지 화면 요청 - userId={}", userId);

        ActiveMode activeMode = userDetails.getActiveMode();
        Long profileId = userDetails.getActiveProfileId();

        int followingCount = relationService.countFollowing(userId, activeMode);
        int followerCount = relationService.countFollower(profileId, activeMode);

        model.addAttribute("nickname", userDetails.getUser().getNickname());
        model.addAttribute("profileMode", activeMode.name());
        model.addAttribute("loginProfileId", userDetails.getActiveProfileId());
        model.addAttribute("profileId", profileId);
        model.addAttribute("followingCount", followingCount);
        model.addAttribute("followerCount", followerCount);

        return "community/mypage/mypage";
    }
}
