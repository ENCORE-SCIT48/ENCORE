package com.encore.encore.domain.member.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformerProfileRequestDto {
    private String stageName;
    private List<String> categories; // 여러 장르를 리스트로 받음
    private String description;
    private String activityArea;
    private String part;
    private String skillLevel;
}
