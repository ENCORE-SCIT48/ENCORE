package com.encore.encore.domain.user.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserJoinRequestDto {
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    private String password;

    @NotBlank(message = "비밀번호확인은 필수 입력값입니다.")
    private String passwordConfirm;

    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    private String nickname;

    @AssertTrue(message = "필수 약관에 동의해야 합니다.")
    private boolean agreeTerms;

    @AssertTrue(message = "개인정보 수집에 동의해야 합니다.")
    private boolean agreePrivacy;

    private boolean agreeMarketing;  // [선택] 마케팅 수신
}
