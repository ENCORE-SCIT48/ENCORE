package com.encore.encore.domain.community.dto;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseCreatePerformerPostDto {

    private Long postId;

    private String postType;

    private String title;

    private LocalDateTime createdAt;
}
