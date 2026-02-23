package com.encore.encore.domain.member.controller;

import com.encore.encore.domain.member.dto.PerformerProfileRequestDto;
import com.encore.encore.domain.member.service.PerformerProfileService;
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

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/performerprofile")
@RequiredArgsConstructor
public class PerformerProfileController {

    private final PerformerProfileService performerProfileService;

    @GetMapping("/view")
    public String viewProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        // 1. 서비스에서 DTO 조회 (우리가 만든 from 메서드 활용)
        PerformerProfileRequestDto profileDto = performerProfileService.getPerformerProfile(userDetails.getUser());

        model.addAttribute("profile", profileDto);
        return "profile/performer-view"; // 위에서 만든 HTML 파일명
    }

    /**
     * 사용자 프로필 설정 및 수정 페이지를 조회합니다.
     *
     * @param userDetails 현재 인증된 사용자 정보 (Spring Security)
     * @param model       뷰에 전달할 데이터를 담는 객체
     * @return performer 프로필 설정 페이지 뷰 이름 (user-setup)
     * @throws ApiException 프로필 정보가 없을 경우 404 에러 발생
     */
    @GetMapping("/setup")
    public String getProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model) {
        // 1. 로그인 여부 체크
        if (userDetails == null) {
            log.warn("[PerformerProfileController] 인증되지 않은 사용자 접근");
            return "redirect:/auth/login"; // 로그인 페이지로 유도
        }
        log.info("[PerformerProfileController] 조회 요청 - User: {}", userDetails.getUsername());

        // 프로필 정보 조회 requestdto 그냥씀
        PerformerProfileRequestDto performerProfileRequestDto
            = performerProfileService.getPerformerProfile(userDetails.getUser());


        model.addAttribute("profile", performerProfileRequestDto);
        // 포지션 옵션 추가
        model.addAttribute("partOptions", List.of("보컬", "기타", "베이스", "드럼", "키보드", "댄서", "기타악기"));
        model.addAttribute("categoryOptions", List.of("버스킹", "밴드", "댄스", "재즈", "클래식", "국악", "마술/퍼포먼스"));
        return "profile/performer-setup"; // 타임리프 파일 경로
    }

    /**
     * [2] 퍼포머 프로필 업데이트
     *
     * @param dto          프로필 입력 데이터
     * @param profileImage 멀티파트 이미지 파일
     * @return 프로필 조회 페이지
     */
    @PostMapping("/setup")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                PerformerProfileRequestDto dto,
                                @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {

        log.info("[PerformerProfileController] 업데이트 요청 - User: {}", userDetails.getUsername());

        // 서비스 호출 (이미지 처리 로직 포함 가능)
        performerProfileService.updatePerformerProfile(userDetails.getUser(), dto, profileImage);

        // 3. 리다이렉트 시 성공 파라미터 추가
        return "redirect:/performerprofile/view?success=true";
    }

}
