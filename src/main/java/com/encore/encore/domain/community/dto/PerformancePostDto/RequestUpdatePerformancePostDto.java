package com.encore.encore.domain.community.dto.PerformancePostDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestUpdatePerformancePostDto {

    private String title;

    private String content;

    private Long performanceId;
}
