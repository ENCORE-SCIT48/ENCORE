package com.encore.encore.domain.member.controller;

import com.encore.encore.domain.member.dto.UserProfileRequestDto;
import com.encore.encore.domain.member.dto.UserProfileResponseDto;
import com.encore.encore.domain.member.service.UserProfileService;
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

import java.util.List;

/**
 * [설명] 일반 관람객(User)의 프로필 조회 및 설정을 관리하는 뷰 컨트롤러입니다.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/userprofile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * [설명] 일반 사용자의 프로필 상세 정보를 조회하여 뷰 페이지를 반환합니다.
     *
     * @param userDetails 현재 인증된 사용자 정보
     * @param model       뷰 전달용 객체
     * @return 관람객 프로필 조회 HTML 경로
     */
    @GetMapping
    public String viewProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model) {

        if (userDetails == null) {
            log.warn("[UserProfileController] 미인증 사용자 접근 시도 - /userprofile");
            return "redirect:/auth/login";
        }

        log.info("[UserProfileController] 관람객 프로필 상세 조회 요청 - User: {}", userDetails.getUsername());

        /// 유저 프로필 정보를 서비스에서 로드하여 모델에 바인딩
        UserProfileResponseDto profile = userProfileService.getUserProfile(userDetails.getUsername());
        model.addAttribute("profile", profile);

        log.info("[UserProfileController] 프로필 조회 완료 - User: {}", userDetails.getUsername());
        return "profile/user-view"; // 앞서 만든 user-view.html로 연결
    }

    /**
     * 사용자 프로필 설정 및 수정 페이지를 조회합니다.
     *
     * @param userDetails 현재 인증된 사용자 정보 (Spring Security)
     * @param model       뷰에 전달할 데이터를 담는 객체
     * @return 유저 설정 페이지 뷰 이름 (user-setup)
     */
    @GetMapping("/setup")
    public String setup(@AuthenticationPrincipal CustomUserDetails userDetails,
                        Model model) {
        if (userDetails == null) {
            log.error("[UserProfileController] 인증 정보 부재로 인한 로그인 페이지 리다이렉트");
            return "redirect:/auth/login";
        }

        log.info("[UserProfileController] 프로필 설정 페이지 진입 - User: {}", userDetails.getUsername()); //db에서 유저 프로필 정보를 가져옴

        // DB에서 현재 사용자의 프로필 정보를 DTO 형태로 가져옴
        UserProfileResponseDto userProfileDto = userProfileService.getUserProfile(userDetails.getUsername());

        // 멀티 선택 옵션 데이터 설정
        List<String> genreOptions = List.of(
            "발라드", "댄스", "힙합/랩", "R&B/Soul", "인디음악",
            "락/메탈", "재즈", "클래식", "EDM", "뮤지컬", "트로트", "국악"
        );

        List<String> typeOptions = List.of(
            "단독콘서트", "페스티벌", "소극장", "버스킹",
            "내한공연", "토크콘서트", "디너쇼", "클럽파티"
        );

        model.addAttribute("profile", userProfileDto);
        model.addAttribute("genreOptions", genreOptions);
        model.addAttribute("typeOptions", typeOptions);

        log.info("[UserProfileController] 설정 폼 및 옵션 리스트 로드 완료");
        return "profile/user-setup";

    }

    /**
     * [설명] 사용자가 입력한 초기 프로필 설정 및 업데이트를 처리합니다.
     *
     * @param userDetails   인증된 사용자 정보
     * @param dto           프로필 수정 데이터 객체 (Bean Validation 적용)
     * @param bindingResult 검증 결과 객체
     * @param profileImage  업로드된 프로필 이미지 파일 (선택)
     * @param model         검증 실패 시 데이터 유지용 모델
     * @return 성공 시 상세 프로필 화면으로 리다이렉트
     */
    @PostMapping("/setup")
    public String setup(@AuthenticationPrincipal CustomUserDetails userDetails,
                        @Valid UserProfileRequestDto dto, // @Valid 추가
                        BindingResult bindingResult,     // 에러 결과를 담는 객체
                        @RequestParam(value = "profileImage", required = false)
                        MultipartFile profileImage,
                        Model model) {

        log.info("[UserProfileController] 프로필 업데이트 시도 - User: {}", userDetails.getUsername());
        // 입력값 검증 실패 시 처리 로직
        if (bindingResult.hasErrors()) {
            log.warn("[UserProfileController] 입력 검증 실패 - User: {}, ErrorCount: {}",
                userDetails.getUsername(), bindingResult.getErrorCount());

            model.addAttribute("profile", dto);
            // 폼 재랜더링 시 필요한 옵션 리스트 다시 바인딩
            model.addAttribute("genreOptions", List.of("발라드", "댄스", "힙합/랩", "R&B/Soul", "인디음악", "락/메탈", "재즈", "클래식", "EDM", "뮤지컬", "트로트", "국악"));
            model.addAttribute("typeOptions", List.of("단독콘서트", "페스티벌", "소극장", "버스킹", "내한공연", "토크콘서트", "디너쇼", "클럽파티"));

            return "profile/user-setup";
        }

        // 서비스 레이어 호출하여 비즈니스 로직 수행
        userProfileService.updateProfile(userDetails.getUsername(), dto, profileImage);

        log.info("[UserProfileController] 프로필 업데이트 최종 완료 - User: {}", userDetails.getUsername());
        return "redirect:/userprofile?success=true";
    }
}
