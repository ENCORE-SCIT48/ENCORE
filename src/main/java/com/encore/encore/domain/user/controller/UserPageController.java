package com.encore.encore.domain.user.controller;

import com.encore.encore.domain.user.dto.UserAccountUpdateRequestDto;
import com.encore.encore.domain.user.service.UserService;
import com.encore.encore.global.config.CustomUserDetails;
import com.encore.encore.global.error.ApiException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserPageController {

    private final UserService userService;


    /**
     * 팔로잉, 팔로워 리스트 페이지 이동
     *
     * @param targetId    팔로잉, 팔로워 리스트를 조회할 대상의 프로필 아이디
     * @param profileMode 팔로잉, 팔로워 리스트를 조회할 대상의 프로필 모드
     * @param tab         먼저 조회가 될 tab(following/follower)
     * @param model       데이터 저장
     * @return relation/following-follower.html
     */
    @GetMapping("/user/follow")
    public String follow(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam("targetId") Long targetId,
        @RequestParam(name = "profileMode", required = false, defaultValue = "USER") String profileMode,
        @RequestParam(name = "tab", required = false, defaultValue = "following") String tab,
        Model model) {

        
        model.addAttribute("loginProfileId", userDetails.getActiveProfileId());
        model.addAttribute("loginProfileMode", userDetails.getActiveMode());

        model.addAttribute("activeTab", tab);
        model.addAttribute("targetId", targetId);
        model.addAttribute("profileMode", profileMode);

        return "relation/following-follower";
    }

    /**
     * 알림 설정 페이지로 이동한다.
     *
     * @return
     */
    @GetMapping("/user/notification")
    public String notification() {
        return "user-settings/notificationSetting";
    }

    @GetMapping("/user/block")
    public String blockList() {
        return "relation/blockList";
    }

    /**
     * 회원정보 수정 페이지 (닉네임, 비밀번호)
     */
    @GetMapping("/user/account")
    public String accountForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null || userDetails.getUser() == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("email", userDetails.getUser().getEmail());
        model.addAttribute("nickname", userDetails.getUser().getNickname());
        model.addAttribute("userAccountUpdate", UserAccountUpdateRequestDto.builder()
            .nickname(userDetails.getUser().getNickname())
            .build());
        return "user-settings/account";
    }

    /**
     * 회원정보 수정 처리 (join과 동일하게 폼 POST → 리다이렉트)
     */
    @PostMapping("/user/account")
    public String accountUpdate(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid UserAccountUpdateRequestDto dto,
        BindingResult bindingResult,
        RedirectAttributes rttr,
        Model model
    ) {
        if (userDetails == null || userDetails.getUser() == null) {
            return "redirect:/auth/login";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("email", userDetails.getUser().getEmail());
            model.addAttribute("nickname", userDetails.getUser().getNickname());
            model.addAttribute("userAccountUpdate", dto);
            return "user-settings/account";
        }
        try {
            userService.updateAccount(userDetails.getUser().getUserId(), dto);
            rttr.addFlashAttribute("message", "회원정보가 수정되었습니다.");
            return "redirect:/mypage";
        } catch (ApiException e) {
            log.warn("회원정보 수정 실패 - userId={}, msg={}", userDetails.getUser().getUserId(), e.getMessage());
            model.addAttribute("message", e.getMessage());
            model.addAttribute("email", userDetails.getUser().getEmail());
            model.addAttribute("nickname", userDetails.getUser().getNickname());
            model.addAttribute("userAccountUpdate", dto);
            return "user-settings/account";
        }
    }
}
