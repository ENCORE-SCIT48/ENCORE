package com.encore.encore.domain.community.entity;

import lombok.Getter;

@Getter
public enum ReportTargetType {
    ROLE_USER("일반 유저"),
    ROLE_PERFORMER("공연자"),
    ROLE_HOST("공연장 호스트"),
    VENUE("공연장"),
    CHAT_ROOM("채팅방"),
    PERFORMER_PARTY("공연자 파티"),
    DM("DM");

    private final String description;

    ReportTargetType(String description) {
        this.description = description;
    }
}
