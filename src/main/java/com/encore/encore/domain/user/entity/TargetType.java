package com.encore.encore.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TargetType {
    USER("유저"),
    PERFORMANCE("공연"),
    VENUE("공연장"),
    CHAT_POST("채팅방");

    // 한국어 이름을 담을 필드
    private final String koreanName;
}
