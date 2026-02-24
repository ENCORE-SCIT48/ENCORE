package com.encore.encore.domain.community.dto.PerformancePostDto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseUpdatePerformancePostDto {

    private Long postId;

    private String postType;

    private String title;

    private String content;

    private Integer capacity;

    private Long performanceId;

    private LocalDateTime updatedAt;
}

