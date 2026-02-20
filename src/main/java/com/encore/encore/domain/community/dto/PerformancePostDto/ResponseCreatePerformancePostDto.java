package com.encore.encore.domain.community.dto.PerformancePostDto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseCreatePerformancePostDto {

    private Long postId;

    private String postType;

    private String title;

    private LocalDateTime createdAt;
}
