package com.encore.encore.domain.community.dto.PerformerPostDto;

import java.util.List;

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

    private List<String> recruitCategory;

    private List<String> recruitPart;
    
    private String recruitArea;
}
