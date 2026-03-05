package com.encore.encore.global.config;

import com.encore.encore.domain.member.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Thymeleaf 뷰에 공통으로 넘길 모델 속성.
 * 로그인 사용자의 현재 프로필 이미지 URL을 currentProfileImageUrl 로 제공.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final ProfileService profileService;

    @ModelAttribute("currentProfileImageUrl")
    public String addCurrentProfileImageUrl(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return null;
        }
        return profileService.getProfileImageUrl(userDetails.getUser(), userDetails.getActiveMode());
    }
}
