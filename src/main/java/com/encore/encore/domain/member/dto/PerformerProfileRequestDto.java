package com.encore.encore.domain.member.dto;

import com.encore.encore.domain.member.entity.PerformerProfile;
import lombok.*;

import java.util.Arrays;
import java.util.Collections;
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
    private String profileImageUrl;
    /**
     * [설명] 엔티티를 응답 DTO로 변환
     */
    public static PerformerProfileRequestDto from(PerformerProfile profile) {
        return PerformerProfileRequestDto.builder()
            .stageName(profile.getStageName())
            // DB에 저장된 "A,B,C" 문자열을 다시 리스트로 변환
            .profileImageUrl(profile.getProfileImageUrl())
            .categories(profile.getCategory() != null ?
                Arrays.asList(profile.getCategory().split(",")) : Collections.emptyList())
            .description(profile.getDescription())
            .activityArea(profile.getActivityArea())
            .part(profile.getPart())
            .skillLevel(profile.getSkillLevel() != null ? profile.getSkillLevel().name() : null)
            .build();
    }
}
