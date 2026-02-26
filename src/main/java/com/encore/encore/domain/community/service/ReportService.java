package com.encore.encore.domain.community.service;

import com.encore.encore.domain.community.dto.report.RequestCreateReportDto;
import com.encore.encore.domain.community.dto.report.ResponseCreateReportDto;
import com.encore.encore.domain.community.entity.Report;
import com.encore.encore.domain.community.repository.ReportRepository;
import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.user.entity.User;
import com.encore.encore.domain.user.repository.UserRepository;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    /**
     * 신고 내용을 저장 합니다.
     *
     * @param dto              신고 내용들
     * @param loginUserId      신고자 userId
     * @param loginProfileMode 신고자 프로필 모드
     * @return 신고 접수 번호, 신고 접수 상태, 신고 접수 날짜
     */
    public ResponseCreateReportDto saveReport(RequestCreateReportDto dto, Long loginUserId, ActiveMode loginProfileMode) {

        User loginUser = userRepository.findById(loginUserId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "유저를 찾을 수 없습니다."));

        Report report = Report.builder()
            .reporter(loginUser)
            .reporterProfileMode(loginProfileMode)
            .targetId(dto.getTargetId())
            .targetType(dto.getTargetType())
            .reason(dto.getReason())
            .reasonDetail(dto.getReasonDetail() != null ? dto.getReasonDetail() : "")
            .build();

        Report savedReport = reportRepository.save(report);
        log.info("신고 저장 완료. ID: {}", savedReport.getReportId());

        return ResponseCreateReportDto.builder()
            .reportId(report.getReportId())
            .createdAt(report.getCreatedAt())
            .status(report.getStatus())
            .build();
    }
}
