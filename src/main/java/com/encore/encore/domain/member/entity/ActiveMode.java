package com.encore.encore.domain.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActiveMode {
    USER("관람객"),
    PERFORMER("공연자"),
    HOST("주최자");

    private final String description;
}
