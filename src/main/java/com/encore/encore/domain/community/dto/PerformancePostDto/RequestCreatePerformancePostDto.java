package com.encore.encore.domain.community.dto.PerformancePostDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestCreatePerformancePostDto {

    private Long performanceId;

    private String title;

    private String content;
}
