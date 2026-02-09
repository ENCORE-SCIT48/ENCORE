package com.encore.encore.domain.community.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseDeletePostDto {

    private Long postId;

    private boolean deleted;
}
