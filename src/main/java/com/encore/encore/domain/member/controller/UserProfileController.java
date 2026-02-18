package com.encore.encore.domain.member.controller;

import com.encore.encore.domain.member.dto.UserProfileRequestDto;
import com.encore.encore.domain.member.dto.UserProfileResponseDto;
import com.encore.encore.domain.member.service.UserProfileService;
import com.encore.encore.global.config.CustomUserDetails;
import com.encore.encore.global.error.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/userprofile")
public class UserProfileController {
    private final UserProfileService userProfileService;

    /**
     * 사용자 프로필 설정 및 수정 페이지를 조회합니다.
     * @param userDetails 현재 인증된 사용자 정보 (Spring Security)
     * @param model       뷰에 전달할 데이터를 담는 객체
     * @return 유저 설정 페이지 뷰 이름 (user-setup)
     * @throws ApiException 프로필 정보가 없을 경우 404 에러 발생
     */
    @GetMapping("/setup")
    public String setup(@AuthenticationPrincipal CustomUserDetails userDetails,
                        Model model) {
        log.info("[UserProfileController] 프로필 조회 요청 - Email: {}", userDetails.getUsername());
        //db에서 유저 프로필 정보를 가져옴
        UserProfileResponseDto userProfileDto = userProfileService.getUserProfile(userDetails.getUsername());
        log.debug("[UserProfileController] 조회된 프로필 데이터 확인 - hasPhoto: {}",
            userProfileDto.getProfileImageUrl() != null);

        //유저 프로필 정보 프론트에  전송
        model.addAttribute("userProfile", userProfileDto);

        return "profile/user-setup";

    }

    /**
     * [설명] 유저의 초기 프로필 설정 및 업데이트를 처리합니다.
     * @param userDetails 인증된 사용자 정보
     * @param dto 프로필 수정 데이터 객체
     * @param profileImage 업로드된 프로필 이미지 파일 (선택 사항)
     * @return 홈 화면으로 리다이렉트
     */
    @PostMapping("/setup")
    public String setup(@AuthenticationPrincipal CustomUserDetails userDetails,
                        UserProfileRequestDto dto,
                        @RequestParam(value = "profileImage", required = false)
                            MultipartFile profileImage) {

        log.info("[UserProfileController] Profile setup request by user: {}", userDetails.getUsername());

        userProfileService.updateProfile(userDetails.getUsername(), dto, profileImage);

        log.info("[UserProfileController] 프로필 설정 완료 - 사용자: {}", userDetails.getUsername());

        return "redirect:/";
    }
}
