package com.encore.encore.domain.community.dto.PerformerPostDto;

import java.util.List;

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

    private List<String> recruitCategory;

    private List<String> recruitPart;

    private String recruitArea;

}
