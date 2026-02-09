package com.encore.encore.domain.user.entity;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE,   // 정상
    PENDING,  // 인증 대기
    BANNED,   // 정지
    DELETED   // 탈퇴
}
