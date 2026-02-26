package com.encore.encore.domain.community.dto.report;

import com.encore.encore.domain.community.entity.Report;
import lombok.*;

import java.time.LocalDateTime;

/**
 * [설명] 신고 완료 후 반환되는 응답 객체입니다.
 *
 * @param reportId  생성된 신고 번호
 * @param status    현재 신고 상태
 * @param createdAt 신고 접수 일시
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseCreateReportDto {
    private Long reportId;
    private Report.ReportStatus status;
    private LocalDateTime createdAt;
}
