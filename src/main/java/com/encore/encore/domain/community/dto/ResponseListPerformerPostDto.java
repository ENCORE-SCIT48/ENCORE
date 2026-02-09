package com.encore.encore.domain.community.dto;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseListPerformerPostDto {

    private Long postId;

    private String postType;

    private String title;

    private String content;

    private Integer viewCount;

    private LocalDateTime createdAt;

}
