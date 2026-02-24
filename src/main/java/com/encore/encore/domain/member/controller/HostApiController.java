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

/**
 * [설명] 호스트 사업자 인증 및 프로필 관련 비동기 처리를 담당하는 API 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/host")
@RequiredArgsConstructor
@Slf4j
public class HostApiController {

    private final BusinessVerificationService businessVerificationService;
    private final HostProfileService hostProfileService;

    /**
     * [Step 1: 사업자 정보 조회]
     * 국세청 API를 통해 입력된 사업자 번호의 유효성 및 상태를 조회합니다.
     *
     * @param dto 사업자 번호가 포함된 요청 DTO
     * @return 조회된 국세청 상태 데이터 (StatusData)
     */
    @PostMapping("/check-status")
    public CommonResponse<NtsStatusResponse.StatusData> checkStatus(@RequestBody HostProfileRequestDto dto) {
        log.info("[HostApiController] [Step 1] 사업자 조회 요청 시작 - 번호: {}", dto.getBusinessNumber());

        // 비즈니스 서비스에 위임하여 국세청 API 조회 실행
        NtsStatusResponse.StatusData data = businessVerificationService.getBusinessStatusData(dto.getBusinessNumber());

        // 데이터가 없거나 국세청에 등록되지 않은 번호인 경우 예외 발생 (ErrorCode.INVALID_REQUEST)
        if (data == null || "국세청에 등록되지 않은 사업자등록번호입니다".equals(data.getB_stt())) {
            log.warn("[HostApiController] 유효하지 않은 사업자 번호 조회 시도: {}", dto.getBusinessNumber());
            throw new ApiException(ErrorCode.INVALID_REQUEST, "등록되지 않은 사업자 번호입니다.");
        }
        log.info("[HostApiController] 사업자 조회 성공 - 상태: {}", data.getB_stt());
        return CommonResponse.ok(data, "사업자 정보가 조회되었습니다. 정보를 확인해 주세요.");
    }

    /**
     * [Step 2: 인증 확정]
     * 조회된 정보를 바탕으로 사용자가 인증을 확정하면 DB의 인증 상태를 업데이트합니다.
     * @param userDetails 현재 인증된 사용자 정보
     * @param dto 인증 처리할 사업자 번호가 포함된 DTO
     * @return 성공 메시지
     */
    @PatchMapping("/verify-confirm")
    public CommonResponse<String> confirmVerification(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                      @RequestBody HostProfileRequestDto dto) {
        log.info("[HostApiController] [Step 2] 인증 확정 시도 - User: {}, BizNum: {}",
            userDetails.getUsername(), dto.getBusinessNumber());


        // 인증 여부(verified=true) 플래그와 번호만 우선적으로 저장 처리
        hostProfileService.markHostAsVerifiedOnly(userDetails.getUser(), dto.getBusinessNumber());

        return CommonResponse.ok(null, "사업자 번호 인증이 확정되었습니다. 나머지 프로필 정보를 입력해주세요.");
    }
}
