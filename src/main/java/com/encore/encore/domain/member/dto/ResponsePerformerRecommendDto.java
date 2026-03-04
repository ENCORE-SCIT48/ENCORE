package com.encore.encore.domain.member.dto;

import com.encore.encore.domain.member.entity.SkillLevel;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponsePerformerRecommendDto {

    private Long userId;
    private Long profileId;
    private String profileMode;
    private String stageName;
    private String profileImageUrl;
    private String part;
    private String category;
    private SkillLevel skillLevel;
    private String activityArea;

}
