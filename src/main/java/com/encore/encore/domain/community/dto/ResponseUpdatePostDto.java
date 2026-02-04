package com.encore.encore.domain.community.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseUpdatePostDto {

    private Long postId;

    private String title;

    private String content;

    private LocalDateTime updatedAt;
}