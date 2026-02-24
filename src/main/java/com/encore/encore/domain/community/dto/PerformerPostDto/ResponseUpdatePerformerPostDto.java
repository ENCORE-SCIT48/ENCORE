package com.encore.encore.domain.community.dto.PerformerPostDto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseUpdatePerformerPostDto {

    private Long postId;

    private String postType;

    private String title;

    private String content;

    private Integer capacity;

    private Long performanceId;

    private Long performerAuthorId;

    private LocalDateTime updatedAt;
}
