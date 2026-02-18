package com.encore.encore.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostProfileRequestDto {

    @NotBlank(message = "단체명(법인명)은 필수 입력 사항입니다.")
    private String organizationName;

    @NotBlank(message = "대표자 성명은 필수 입력 사항입니다.")
    private String representativeName;

    /**
     * [설명] 사업자 등록 번호
     * [검증] 하이픈 포함 여부와 관계없이 10자리 숫자 형식을 체크합니다.
     */
    @NotBlank(message = "사업자 등록 번호는 필수 입력 사항입니다.")
    @Pattern(regexp = "^\\d{3}-?\\d{2}-?\\d{5}$", message = "올바른 사업자 번호 형식이 아닙니다.")
    private String businessNumber;

    /**
     * [설명] 개업 일자 (YYYYMMDD)
     * [검증] 국세청 API 규격인 8자리 숫자를 확인합니다.
     */
    @NotBlank(message = "개업 일자는 필수 입력 사항입니다.")
    @Pattern(regexp = "^\\d{4}\\d{2}\\d{2}$", message = "개업 일자는 YYYYMMDD 형식으로 입력해주세요.")
    private String openingDate;

    @NotBlank(message = "대표 연락처는 필수 입력 사항입니다.")
    private String contactNumber;

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @NotBlank(message = "비즈니스 이메일은 필수 입력 사항입니다.")
    private String businessEmail;
}
