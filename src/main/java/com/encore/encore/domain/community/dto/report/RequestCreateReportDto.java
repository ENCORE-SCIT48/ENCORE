package com.encore.encore.domain.community.dto.report;

import com.encore.encore.domain.community.entity.Report;
import com.encore.encore.domain.community.entity.ReportTargetType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestCreateReportDto {

    private Long targetId;
    private ReportTargetType targetType;
    private Report.ReportReason reason;
    private String reasonDetail;
}
