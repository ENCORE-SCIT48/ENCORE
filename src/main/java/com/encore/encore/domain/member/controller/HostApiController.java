package com.encore.encore.domain.member.controller;

import com.encore.encore.domain.member.dto.HostProfileRequestDto;
import com.encore.encore.domain.member.service.HostProfileService;
import com.encore.encore.global.business.BusinessVerificationService;
import com.encore.encore.global.common.CommonResponse;
import com.encore.encore.global.config.CustomUserDetails;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController // 데이터를 반환해야 하므로 RestController가 편합니다.
@RequestMapping("/api/host")
@RequiredArgsConstructor
@Slf4j
public class HostApiController {

    private final BusinessVerificationService businessVerificationService;
    private final HostProfileService hostProfileService;

    /**
     * [사업자 진위 확인 API]
     * 사용자가 입력한 정보를 국세청 API로 검증하고, 성공 시 DB의 is_verified를 true로 변경
     */
    @PatchMapping("/verify-business")
    public CommonResponse<String> verifyBusiness(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @RequestBody HostProfileRequestDto dto) {

        log.info("[BusinessVerify] 인증 시도 - User: {}, B_NO: {}",
            userDetails.getUsername(),
            dto.getBusinessNumber());

        // 1. 국세청 API 호출 (하이픈 제거 후 전달)
        boolean isSuccess = businessVerificationService.verifyBusiness(
            dto.getBusinessNumber().replaceAll("-", ""),
            dto.getOpeningDate().replaceAll("-", ""),
            dto.getRepresentativeName()
        );

        if (isSuccess) {
            // 2. 인증 성공 시 DB 업데이트
            hostProfileService.markHostAsVerified(userDetails.getUser(),dto);
            return CommonResponse.ok(null, "사업자 인증에 성공하였습니다.");
        } else {
            // 3. 인증 실패 시
            throw new ApiException(ErrorCode.INVALID_REQUEST, "정보가 일치하지 않습니다.");
        }
    }
}
