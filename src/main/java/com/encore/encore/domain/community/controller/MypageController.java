package com.encore.encore.domain.community.controller;

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

    /**
     * [설명] 통합 마이페이지 화면을 조회합니다.
     *
     * @param userDetails 로그인 사용자 정보
     * @param model       화면 전달 객체
     * @return 마이페이지 화면
     */
    @GetMapping("/mypage")
    public String mypage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        log.info("[MypageController] 마이페이지 화면 요청 - userId={}",
                userDetails.getUser().getUserId());
                

        // 기본 프로필 정보 세팅 (현재는 최소 구조)
        model.addAttribute("nickname", userDetails.getUser().getNickname());
        model.addAttribute("role", userDetails.getUser().getRole());

        return "community/mypage/mypage";
    }
}