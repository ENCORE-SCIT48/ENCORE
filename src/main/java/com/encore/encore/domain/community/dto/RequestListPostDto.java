package com.encore.encore.domain.community.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestListPostDto {

    private String postType;

    private Long performanceId;
}
