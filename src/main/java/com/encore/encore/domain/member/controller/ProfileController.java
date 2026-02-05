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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * 프로필 전환 기능
     * - 현재 사용자의 ActiveMode(USER, PERFORMER, HOST)를 변경합니다.
     * @param mode        바꾸고 싶은 프로필
     * @param userDetails 현재 유저 정보
     * @param session
     * @return
     */
    @PostMapping("/switch")
    public String switchMode(@RequestParam ActiveMode mode, @AuthenticationPrincipal CustomUserDetails userDetails, HttpSession session) {
        log.info("[Mode Switch] 요청 수신 - 유저: {}, 변경하려는 모드: {}", userDetails.getUsername(), mode);

           // 1. 서비스를 통해 프로필 ID 조회 (비즈니스 로직 분리)
           Long profileId = profileService.findProfileIdByMode(mode, userDetails.getUser());
           log.info("[Mode Switch] 프로필 조회 성공 - ProfileID: {}", profileId);

           // 2. UserDetails 내부 필드 업데이트
           userDetails.updateActiveProfile(mode, profileId);

           // 3. SecurityContext 갱신 및 세션 동기화
           Authentication newAuth = new UsernamePasswordAuthenticationToken(
               userDetails, userDetails.getPassword(), userDetails.getAuthorities());
           SecurityContextHolder.getContext().setAuthentication(newAuth);

           session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

           log.info("[Mode Switch] 세션 갱신 완료 - 유저: {} 가 현재 {} 모드로 활동 중", userDetails.getUsername(), mode);

           return "redirect:/";

    }
}
