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

    private Long venueId;

    private Long performanceId;

    private Long performerId;

    private String venueName;

    private String venueAddress;

    private String venueType;

    private String venueImage;

    private Long hostId;

    private String title;

    private String content;

    private Integer capacity;

    private Integer approvedCount;

    private Integer viewCount;

    private LocalDateTime createdAt;
}
