package com.encore.encore.domain.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActiveMode {
    ROLE_USER("관람객"),
    ROLE_PERFORMER("공연자"),
    ROLE_HOST("주최자");

    private final String description;


}
