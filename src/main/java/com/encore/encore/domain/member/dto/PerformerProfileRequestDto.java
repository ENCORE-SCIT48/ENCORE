package com.encore.encore.domain.member.dto;

import com.encore.encore.domain.member.entity.PerformerProfile;
import lombok.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformerProfileRequestDto {
    private String stageName;
    @Builder.Default // 빌더 사용 시에도 기본값 유지
    private List<String> categories = new ArrayList<>();
    private String description;
    private String activityArea;
    // String part를 List<String>으로 변경
    @Builder.Default
    private List<String> part = new ArrayList<>();
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
                new ArrayList<>(Arrays.asList(profile.getCategory().split(","))) : new ArrayList<>())
            .description(profile.getDescription())
            .activityArea(profile.getActivityArea())
            // 포지션(part) 변환 로직 추가
            .part(profile.getPart() != null ?
                new ArrayList<>(Arrays.asList(profile.getPart().split(","))) : new ArrayList<>())
            .skillLevel(profile.getSkillLevel() != null ? profile.getSkillLevel().name() : null)
            .build();
    }
}
