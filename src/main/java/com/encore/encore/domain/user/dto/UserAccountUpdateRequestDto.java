package com.encore.encore.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 회원정보 수정 요청 DTO.
 * - 닉네임: 변경 시 2~10자
 * - 비밀번호 변경 시: 현재 비밀번호 필수, 새 비밀번호·확인 일치
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccountUpdateRequestDto {

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 10, message = "닉네임은 2~10자 이내로 입력해주세요.")
    private String nickname;

    /** 비밀번호를 변경할 때만 입력. 비어 있으면 변경 안 함 */
    private String currentPassword;

    /** 새 비밀번호. 비어 있으면 변경 안 함 */
    private String newPassword;

    /** 새 비밀번호 확인 */
    private String newPasswordConfirm;
}
