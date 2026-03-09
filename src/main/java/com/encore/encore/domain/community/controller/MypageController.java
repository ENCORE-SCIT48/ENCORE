package com.encore.encore.domain.community.controller;

import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.member.service.ProfileService;
import com.encore.encore.domain.user.service.RelationService;
import com.encore.encore.global.config.CustomUserDetails;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MypageController {

    private final RelationService relationService;
    private final ProfileService profileService;

    /**
     * [설명] 통합 마이페이지 화면을 조회합니다.
     * profileId가 null이면 활성 모드로 프로필 ID를 조회해 세션에 반영하고, 해당 모드 프로필이 없으면 프로필 선택으로 리다이렉트합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @param model       화면 전달 객체
     * @return 마이페이지 화면 경로 또는 리다이렉트
     */
    @GetMapping("/mypage")
    public String mypage(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        Model model,
        HttpSession session) {

        if (userDetails == null) {
            log.info("[MypageController] 비로그인 사용자의 마이페이지 접근");
            return "redirect:/auth/login";
        }

        Long userId = userDetails.getUser().getUserId();
        log.info("[MypageController] 마이페이지 화면 요청 - userId={}", userId);

        ActiveMode activeMode = userDetails.getActiveMode();
        Long profileId = userDetails.getActiveProfileId();

        if (profileId == null) {
            try {
                profileId = profileService.findProfileIdByMode(activeMode, userDetails.getUser());
                userDetails.updateActiveProfile(activeMode, profileId);
                Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    userDetails, userDetails.getPassword(), userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(newAuth);
                session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            } catch (Exception e) {
                log.warn("[MypageController] 활성 프로필 조회 실패 - redirect to select. activeMode={}", activeMode, e);
                return "redirect:/profiles/select?error=no_profile";
            }
        }

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
