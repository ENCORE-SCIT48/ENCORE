package com.encore.encore.domain.community.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestUpdatePostDto {

    private Long postId;

    private String title;

    private String content;
}
