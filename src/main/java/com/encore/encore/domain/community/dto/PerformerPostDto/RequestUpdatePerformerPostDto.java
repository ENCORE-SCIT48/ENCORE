package com.encore.encore.domain.community.dto.PerformerPostDto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestUpdatePerformerPostDto {

    private String title;

    private String content;

    private Long performanceId;

    private Integer capacity;
}
