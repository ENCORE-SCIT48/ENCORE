package com.encore.encore.domain.member.controller;

import com.encore.encore.domain.member.dto.HostProfileRequestDto;
import com.encore.encore.domain.member.dto.HostProfileResponseDto;
import com.encore.encore.domain.member.service.HostProfileService;
import com.encore.encore.global.config.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/hostprofile")
@RequiredArgsConstructor
@Slf4j
public class HostProfileController {

    private final HostProfileService hostProfileService;
    /**
     * 호스트 프로필 조회 페이지 (View 전용)
     * URL: GET /hostprofile/view
     */
    @GetMapping("/view")
    public String viewHostProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  Model model) {

        log.info("[HostProfileController] 프로필 조회 요청 - User: {}", userDetails.getUsername());

        // 1. 서비스에서 호스트 프로필 정보를 DTO 형태로 조회
        // (이미 구현하신 getHostProfile 또는 별도의 ResponseDto 반환 메서드 활용)
        HostProfileResponseDto profileDto = hostProfileService.getHostProfile(userDetails.getUser());

        // 2. 모델에 담아서 뷰로 전달
        model.addAttribute("profile", profileDto);

        // 3. 조회 전용 HTML 파일명 (src/main/resources/templates/profile/host-view.html)
        return "profile/host-view";
    }

    /**
     * 호스트 프로필 설정 페이지 조회
     * @param userDetails 현재 인증된 유저 정보
     * @param model 뷰 전달용 객체
     * @return 호스트 설정 뷰 경로
     */
    @GetMapping("/setup")
    public String setupPage(@AuthenticationPrincipal CustomUserDetails userDetails,
                            Model model) {
        log.info("[HostProfileController] 조회 요청 - User: {}", userDetails.getUsername());

        // 1. 현재 로그인한 유저의 호스트 프로필 정보 가져오기
        HostProfileResponseDto profileResponse =
            hostProfileService.getHostProfile(userDetails.getUser());
        log.info("[HostProfileController] 호스트 프로필 조회 완료 - profileResponse: {}", profileResponse);
        // 2. 뷰로 데이터 전달
        model.addAttribute("profile", profileResponse);

        return "profile/host-setup";
    }

    /**
     * 호스트 프로필 업데이트 처리
     * @param userDetails 현재 인증된 유저 정보
     * @param dto 수정 요청 데이터 (Validation 적용)
     * @param bindingResult 검증 결과
     * @param profileImage 업로드 이미지 파일 (선택)
     * @return 설정 페이지 리다이렉트
     */
    @PostMapping("/setup")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @Valid HostProfileRequestDto dto,
                                BindingResult bindingResult,
                                @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                                Model model) {

        log.info("[HostProfileController] 업데이트 시도 - User: {}", userDetails.getUsername());

        // 1. 유효성 검사 실패 시 처리
        if (bindingResult.hasErrors()) {
            log.warn("[HostProfileController] 검증 에러 발생: {}", bindingResult.getAllErrors());

            // 검증 실패 시 기존 데이터를 다시 모델에 담아 입력 폼 유지
            model.addAttribute("profile", dto);

            return "profile/host-setup";
        }

        // 3. 서비스 호출하여 업데이트 수행
        hostProfileService.updateHostProfile(userDetails.getUser(), dto, profileImage);

        log.info("[HostProfileController] 업데이트 완료 - User: {}", userDetails.getUsername());

        // 4. 완료 후 리다이렉트 (새로고침 중복 방지)
        return "redirect:/hostprofile/view?success=true";
    }



}
