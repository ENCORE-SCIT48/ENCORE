package com.encore.encore.domain.community.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestCreatePerformerPostDto {

    private Long performanceId;

    private String postType;

    private String title;

    private String content;

}
