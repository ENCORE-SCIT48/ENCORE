package com.encore.encore.domain.community.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestCreatePostDto {

    private Long performanceId;

    private String postType;

    private String title;

    private String content;
    
}
