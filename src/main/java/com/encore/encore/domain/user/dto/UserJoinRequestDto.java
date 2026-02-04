package com.encore.encore.domain.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserJoinRequestDto {
    private String email;
    private String password;
    private String passwordConfirm;
    private String nickname;
}
