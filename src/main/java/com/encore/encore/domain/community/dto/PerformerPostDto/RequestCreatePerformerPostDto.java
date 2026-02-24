package com.encore.encore.domain.community.dto.PerformerPostDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestCreatePerformerPostDto {

    private Long performanceId;

    private String title;

    private String content;

    private Integer capacity;

}
