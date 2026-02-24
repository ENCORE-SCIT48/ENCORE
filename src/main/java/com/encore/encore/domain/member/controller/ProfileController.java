package com.encore.encore.domain.member.controller;

import com.encore.encore.domain.member.entity.ActiveMode;

import com.encore.encore.domain.member.service.HostProfileService;
import com.encore.encore.domain.member.service.PerformerProfileService;
import com.encore.encore.domain.member.service.ProfileService;
import com.encore.encore.domain.member.service.UserProfileService;
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
import org.springframework.web.bind.annotation.*;

/**
 * 사용자의 프로필 선택 및 모드 전환을 관리하는 컨트롤러입니다.
 */
@Slf4j
@Controller
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final UserProfileService userProfileService;
    private final HostProfileService hostProfileService;
    private final PerformerProfileService performerProfileService;

    /**
     * [화면] 프로필 선택 메인 페이지로 이동합니다.
     * @return 프로필 선택 뷰 경로
     */
    @GetMapping("/select")
    public String selectPage(@AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model) {
        if (userDetails == null) return "redirect:/auth/login";
        model.addAttribute("userProfile", userProfileService.getUserProfile(userDetails.getUsername()));
        model.addAttribute("performerProfile", performerProfileService.getPerformerProfile(userDetails.getUser()));
        model.addAttribute("hostProfile", hostProfileService.getHostProfile(userDetails.getUser()));
        return "profile/select";
    }


    /**
     * 사용자의 활성 프로필을 전환하고 세션을 갱신합니다.
     * @param mode        변경하고자 하는 프로필 (USER, PERFORMER, HOST)
     * @param userDetails 현재 로그인한 사용자의 상세 정보
     * @param session
     * @return
     */
    @PostMapping("/switch")
    public String switchMode(
        @RequestParam ActiveMode mode,
        @AuthenticationPrincipal CustomUserDetails userDetails,
        HttpSession session) {
        // 로그인 체크 (NPE 방지)
        if (userDetails == null) {
            log.warn("[Mode Switch] 로그인 정보 없음.");
            return "redirect:/auth/login";
        }
        // 1. 데이터 조회
        Long profileId = profileService.findProfileIdByMode(mode, userDetails.getUser());
        boolean isInitialized = profileService.checkIfInitialized(mode, profileId);

        log.debug("[Mode Switch] Profile search result - ID: {}, Initialized: {}", profileId, isInitialized);

        // 2. 권한 및 세션 갱신
        userDetails.updateActiveProfile(mode, profileId);

        Authentication newAuth = new UsernamePasswordAuthenticationToken(
            userDetails, userDetails.getPassword(), userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(newAuth);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        // 유저명, 선택한 모드, 신규 유저 여부
        log.info("[Mode Switch] User: '{}' switched to '{}' (NewUser: {})",
            userDetails.getUsername(), mode, !isInitialized);

        // 4. 초기화 여부에 따른 리다이렉트
        if (!isInitialized) {

            // mode.name()이 "ROLE_USER"라면 "user"로 변환
            String cleanedMode = mode.name().replace("ROLE_", "").toLowerCase();

            log.info("[Mode Switch] Redirecting to setup page: /profiles/{}/setup", cleanedMode);
            return switch (mode) {
                // UserProfileController의 @RequestMapping("/userprofile") 로 이동
                case ROLE_USER -> "redirect:/userprofile/setup";

                // PerformerProfileController의 @RequestMapping("/performerprofile") 로 이동
                // (Performer 컨트롤러의 @GetMapping이 기본 경로이므로 /setup을 붙이지 않음)
                case ROLE_PERFORMER -> "redirect:/performerprofile/setup";

                // HostProfileController의 @RequestMapping("/hostprofile") 로 이동
                // (Host 컨트롤러 역시 @GetMapping이 기본 경로임)
                case ROLE_HOST -> "redirect:/hostprofile/setup";
            };
        }
        return "redirect:/";
    }
}
