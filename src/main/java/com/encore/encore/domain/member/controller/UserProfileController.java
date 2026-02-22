package com.encore.encore.domain.member.controller;

import com.encore.encore.domain.member.dto.UserProfileRequestDto;
import com.encore.encore.domain.member.dto.UserProfileResponseDto;
import com.encore.encore.domain.member.service.UserProfileService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/userprofile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * [추가] 사용자 프로필 상세 조회 페이지 (View Mode)
     * URL: GET /userprofile
     */
    @GetMapping
    public String viewProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model) {
        if (userDetails == null) return "redirect:/auth/login";

        log.info("[UserProfileController] 프로필 상세 조회 - User: {}", userDetails.getUsername());

        // 서비스에서 데이터를 가져와 "profile"이라는 이름으로 모델에 담습니다.
        UserProfileResponseDto profile = userProfileService.getUserProfile(userDetails.getUsername());
        model.addAttribute("profile", profile);

        return "profile/user-view"; // 앞서 만든 user-view.html로 연결
    }

    /**
     * 사용자 프로필 설정 및 수정 페이지를 조회합니다.
     *
     * @param userDetails 현재 인증된 사용자 정보 (Spring Security)
     * @param model       뷰에 전달할 데이터를 담는 객체
     * @return 유저 설정 페이지 뷰 이름 (user-setup)
     * @throws ApiException 프로필 정보가 없을 경우 404 에러 발생
     */
    @GetMapping("/setup")
    public String setup(@AuthenticationPrincipal CustomUserDetails userDetails,
                        Model model) {
        // 1. 방어 코드 추가
        if (userDetails == null) {
            log.error("[UserProfileController] 유저 정보가 없습니다! 로그인 페이지로 튕깁니다.");
            return "redirect:/auth/login";
        }

        log.info("[UserProfileController] 프로필 조회 요청 - Email: {}", userDetails.getUsername());
        //db에서 유저 프로필 정보를 가져옴
        UserProfileResponseDto userProfileDto = userProfileService.getUserProfile(userDetails.getUsername());
        // 2. 전체 선택지 리스트 정의 (보통 Enum이나 공통 상수로 관리하면 더 좋습니다)
        List<String> genreOptions = List.of(
            "발라드", "댄스", "힙합/랩", "R&B/Soul", "인디음악",
            "락/메탈", "재즈", "클래식", "EDM", "뮤지컬", "트로트", "국악"
        );

        List<String> typeOptions = List.of(
            "단독콘서트", "페스티벌", "소극장", "버스킹",
            "내한공연", "토크콘서트", "디너쇼", "클럽파티"
        );
        log.debug("[UserProfileController] 조회된 프로필 데이터 확인 - hasPhoto: {}",
            userProfileDto.getProfileImageUrl() != null);

        //유저 프로필 정보 프론트에  전송
        model.addAttribute("profile", userProfileDto);
        model.addAttribute("genreOptions", genreOptions);
        model.addAttribute("typeOptions", typeOptions);

        return "profile/user-setup";

    }

    /**
     * [설명] 유저의 초기 프로필 설정 및 업데이트를 처리합니다.
     *
     * @param userDetails  인증된 사용자 정보
     * @param dto          프로필 수정 데이터 객체
     * @param profileImage 업로드된 프로필 이미지 파일 (선택 사항)
     * @return 관람객 프로필 화면으로 이동
     */
    @PostMapping("/setup")
    public String setup(@AuthenticationPrincipal CustomUserDetails userDetails,
                        @Valid UserProfileRequestDto dto, // @Valid 추가
                        BindingResult bindingResult,     // 에러 결과를 담는 객체
                        @RequestParam(value = "profileImage", required = false)
                        MultipartFile profileImage,
                        Model model) {

        if (bindingResult.hasErrors()) {
            // 에러 발생 시 기존 선택지 리스트를 다시 모델에 담아 페이지를 새로 렌더링
            model.addAttribute("profile", dto);
            List<String> genreOptions = List.of(
                "발라드", "댄스", "힙합/랩", "R&B/Soul", "인디음악",
                "락/메탈", "재즈", "클래식", "EDM", "뮤지컬", "트로트", "국악"
            );

            List<String> typeOptions = List.of(
                "단독콘서트", "페스티벌", "소극장", "버스킹",
                "내한공연", "토크콘서트", "디너쇼", "클럽파티"
            );
            return "profile/user-setup";
        }

        log.info("[UserProfileController] Profile setup request by user: {}", userDetails.getUsername());

        userProfileService.updateProfile(userDetails.getUsername(), dto, profileImage);
        log.info("[UserProfileController] 프로필 설정 완료 - 사용자: {}", userDetails.getUsername());

        return "redirect:/userprofile?success=true";
    }
}
