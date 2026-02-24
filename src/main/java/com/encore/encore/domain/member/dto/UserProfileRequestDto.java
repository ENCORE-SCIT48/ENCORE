package com.encore.encore.domain.member.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileRequestDto {
    @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식(010-0000-0000)이어야 합니다.")
    private String phoneNumber;

    @NotNull(message = "생년월일을 선택해주세요.")
    @Past(message = "생년월일은 과거 날짜여야 합니다.")
    private LocalDate birthDate;

    @NotBlank(message = "활동 선호 지역을 선택해주세요.")
    private String location;

    @Size(max = 200, message = "자기소개는 200자 이내로 작성해주세요.")
    private String introduction;

    @NotEmpty(message = "선호 장르를 최소 하나 이상 선택해주세요.")
    private List<String> preferredGenres;

    @NotEmpty(message = "선호 공연 형태를 최소 하나 이상 선택해주세요.")
    private List<String> preferredPerformanceTypes;
}
