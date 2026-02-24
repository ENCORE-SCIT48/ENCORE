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

/**
 * [설명] 퍼포머(공연자) 프로필의 조회 및 설정을 관리하는 뷰 컨트롤러입니다.
 */
@Slf4j
@Controller
@RequestMapping("/performerprofile")
@RequiredArgsConstructor
public class PerformerProfileController {

    private final PerformerProfileService performerProfileService;

    /**
     * [설명] 퍼포머 프로필 상세 정보를 조회하여 뷰 페이지를 반환합니다.
     * @param userDetails 현재 인증된 사용자 정보
     * @param model 뷰 전달용 객체
     * @return 퍼포머 프로필 조회 HTML 경로
     */
    @GetMapping("/view")
    public String viewProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        log.info("[PerformerProfileController] 프로필 상세 조회 요청 - User: {}", userDetails.getUsername());

        // 서비스 레이어에서 퍼포머 프로필 엔티티를 DTO로 변환하여 로드
        PerformerProfileRequestDto profileDto =
            performerProfileService.getPerformerProfile(userDetails.getUser());

        model.addAttribute("profile", profileDto);

        log.info("[PerformerProfileController] 상세 정보 로드 완료 - User: {}", userDetails.getUsername());
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
        // 인증 여부 체크 및 로그 기록
        if (userDetails == null) {
            log.warn("[PerformerProfileController] 미인증 사용자 접근 시도");
            return "redirect:/auth/login"; // 로그인 페이지로 유도
        }

        log.info("[PerformerProfileController] 프로필 설정 페이지 진입 - User: {}", userDetails.getUsername());

        // 기존 프로필 정보 조회
        PerformerProfileRequestDto performerProfileRequestDto
            = performerProfileService.getPerformerProfile(userDetails.getUser());

        model.addAttribute("profile", performerProfileRequestDto);

        // 구현 주석: 화면의 체크박스 및 선택 리스트를 위한 옵션 데이터 전달
        model.addAttribute("partOptions", List.of("보컬", "기타", "베이스", "드럼", "키보드", "댄서", "기타악기"));
        model.addAttribute("categoryOptions", List.of("버스킹", "밴드", "댄스", "재즈", "클래식", "국악", "마술/퍼포먼스"));

        log.info("[PerformerProfileController] 설정 폼 옵션 데이터 로드 완료");
        return "profile/performer-setup"; // 타임리프 파일 경로
    }

    /**
     * [설명] 입력된 퍼포머 프로필 정보와 이미지 파일을 업데이트합니다.
     * @param userDetails 현재 인증된 사용자 정보
     * @param dto 프로필 수정 요청 데이터
     * @param profileImage 업로드된 이미지 파일 (선택)
     * @return 프로필 조회 페이지로 리다이렉트
     */
    @PostMapping("/setup")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                PerformerProfileRequestDto dto,
                                @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {

        log.info("[PerformerProfileController] 프로필 업데이트 프로세스 시작 - User: {}", userDetails.getUsername());
        // 이미지 저장 로직을 포함한 업데이트 요청
        performerProfileService.updatePerformerProfile(userDetails.getUser(), dto, profileImage);

        log.info("[PerformerProfileController] 프로필 업데이트 최종 완료 - User: {}", userDetails.getUsername());

        return "redirect:/performerprofile/view?success=true";
    }

}
