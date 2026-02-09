package com.encore.encore.domain.member.controller;

import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.member.service.ProfileService;
import com.encore.encore.global.config.CustomUserDetails;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
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

    /**
     * [화면] 프로필 선택 메인 페이지로 이동합니다.
     * @return 프로필 선택 뷰 경로
     */
    @GetMapping("/select")
    public String selectPage() {
        return "profile/select";
    }

    /**
     * [화면] 각 모드별 상세 설정 페이지로 이동합니다.
     */
    @GetMapping("/{mode}/setup")
    public String setupPage(@PathVariable String mode) {
        return "profile/" + mode + "-setup";
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
            log.info("[Mode Switch] Redirecting to setup page for user: '{}'", userDetails.getUsername());
            return "redirect:/profiles/" + mode.name().toLowerCase() + "/setup";
        }

        return "redirect:/";
    }
}
