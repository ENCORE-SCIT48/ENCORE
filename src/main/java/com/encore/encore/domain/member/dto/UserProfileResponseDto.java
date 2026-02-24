package com.encore.encore.domain.member.dto;

import com.encore.encore.domain.member.entity.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@Getter
@Builder
@ToString
public class UserProfileResponseDto {
    private final String phoneNumber;
    private final LocalDate birthDate;
    private final String location;
    private final String introduction;
    private final List<String> preferredGenres;
    private final List<String> preferredPerformanceTypes;
    private final String profileImageUrl; // 화면에 이미지를 띄워줘야 하니까 추가

    public static UserProfileResponseDto from(UserProfile entity) {
        return UserProfileResponseDto.builder()
            .phoneNumber(entity.getPhoneNumber())
            .birthDate(entity.getBirthDate())
            .location(entity.getLocation())
            .introduction(entity.getIntroduction())
            .profileImageUrl(entity.getProfileImageUrl())
            // DB의 "ROCK,POP" 문자열을 다시 리스트로 쪼개서 담음
            .preferredGenres(splitToList(entity.getPreferredGenres()))
            .preferredPerformanceTypes(splitToList(entity.getPreferredPerformanceTypes()))
            .build();
    }

    private static List<String> splitToList(String str) {
        if (str == null || str.isEmpty()) return List.of();
        return Arrays.asList(str.split(","));
    }
}
