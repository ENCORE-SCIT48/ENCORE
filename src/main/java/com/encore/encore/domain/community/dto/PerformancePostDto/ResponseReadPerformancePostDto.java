package com.encore.encore.domain.community.dto.PerformancePostDto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseReadPerformancePostDto {

    private Long postId;

    private String postType;

    private Long performanceId;

    private Long performerAuthorId;

    private String title;

    private String content;

    private Integer viewCount;

    private LocalDateTime createdAt;
}
