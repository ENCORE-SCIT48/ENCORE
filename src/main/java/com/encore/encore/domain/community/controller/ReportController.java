package com.encore.encore.domain.community.controller;

import com.encore.encore.domain.community.dto.report.RequestCreateReportDto;
import com.encore.encore.domain.community.dto.report.ResponseCreateReportDto;
import com.encore.encore.domain.community.entity.Report;
import com.encore.encore.domain.community.entity.ReportTargetType;
import com.encore.encore.domain.community.service.ReportService;
import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.global.common.CommonResponse;
import com.encore.encore.global.config.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;

    /**
     * [설명] 신고하기 페이지를 출력합니다.
     *
     * @param targetId   신고 대상 ID
     * @param targetType 신고 대상 타입
     * @param targetName 신고 대상 표시 명칭(유저 신고면 유저 이름, 공연이면 공연 타이틀 등)
     * @return 신고 페이지 뷰 경로
     */
    @GetMapping("/report")
    public String getReportPage(@RequestParam Long targetId,
                                @RequestParam ReportTargetType targetType,
                                @RequestParam String targetName,
                                Model model) {
        log.info("신고 페이지 요청 - 대상: {}, 타입: {}", targetName, targetType);

        // targetType이 프로필 타입 구분일 경우 유저로 통일
        String targetLabel = (targetType == ReportTargetType.ROLE_USER ||
            targetType == ReportTargetType.ROLE_PERFORMER ||
            targetType == ReportTargetType.ROLE_HOST)

            ? "유저" : targetType.getDescription();

        model.addAttribute("targetId", targetId);
        model.addAttribute("targetType", targetType);
        model.addAttribute("targetLabel", targetLabel);
        model.addAttribute("targetName", targetName);
        model.addAttribute("reasons", Report.ReportReason.values());

        return "community/reportForm";
    }

    /**
     * [설명] 신고 접수를 처리하는 API입니다.
     *
     * @param dto 신고 요청 정보
     * @return 공통 응답 객체
     */
    @PostMapping("/api/community/reports")
    @ResponseBody
    public ResponseEntity<CommonResponse<ResponseCreateReportDto>> createReport(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody RequestCreateReportDto dto) {

        log.info("신고 접수 시작 - TargetID: {}, Reason: {}", dto.getTargetId(), dto.getReason());

//        ActiveMode loginProfileMode = userDetails.getActiveMode();
//        Long loginUserId = userDetails.getUser().getUserId();

        ActiveMode loginProfileMode = ActiveMode.ROLE_HOST;
        Long loginUserId = 2L;

        ResponseCreateReportDto result = reportService.saveReport(dto, loginUserId, loginProfileMode);

        return ResponseEntity.ok(CommonResponse.ok(result, "신고 접수 완료"));
    }
}
