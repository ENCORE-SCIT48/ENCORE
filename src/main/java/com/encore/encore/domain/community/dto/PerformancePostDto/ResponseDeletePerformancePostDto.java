package com.encore.encore.domain.community.dto.PerformancePostDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseDeletePerformancePostDto {

    private Long postId;

    private boolean deleted;
}
