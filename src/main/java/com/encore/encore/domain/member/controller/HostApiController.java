package com.encore.encore.domain.member.controller;

import com.encore.encore.domain.member.dto.HostProfileRequestDto;
import com.encore.encore.domain.member.service.HostProfileService;
import com.encore.encore.global.business.BusinessVerificationService;
import com.encore.encore.global.business.NtsStatusResponse;
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
         * [Step 1: 사업자 정보 조회]
         * 번호를 입력하고 '조회' 버튼을 눌렀을 때 실행.
         * DB 저장 없이 국세청 정보만 가져와서 프론트에 보여줌.
         */
        @PostMapping("/check-status")
        public CommonResponse<NtsStatusResponse.StatusData> checkStatus(@RequestBody HostProfileRequestDto dto) {
            log.info("[Step 1] 사업자 조회 시도: {}", dto.getBusinessNumber());

            NtsStatusResponse.StatusData data = businessVerificationService.getBusinessStatusData(dto.getBusinessNumber());

            if (data == null || "국세청에 등록되지 않은 사업자등록번호입니다".equals(data.getB_stt())) {
                throw new ApiException(ErrorCode.INVALID_REQUEST, "등록되지 않은 사업자 번호입니다.");
            }

            return CommonResponse.ok(data, "사업자 정보가 조회되었습니다. 정보를 확인해 주세요.");
        }

    @PatchMapping("/verify-confirm")
    public CommonResponse<String> confirmVerification(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                     @RequestBody HostProfileRequestDto dto) {

        log.info("[Step 2] 인증 확정 클릭 - User: {}", userDetails.getUsername());

        // 인증 상태만 true로 바꾸고 번호만 저장
        hostProfileService.markHostAsVerifiedOnly(userDetails.getUser(), dto.getBusinessNumber());

        return CommonResponse.ok(null, "사업자 번호 인증이 확정되었습니다. 나머지 프로필 정보를 입력해주세요.");
    }
}
