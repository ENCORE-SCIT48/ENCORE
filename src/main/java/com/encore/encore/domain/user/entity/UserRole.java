package com.encore.encore.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    USER("ROLE_USER"),
    ARTIST("ROLE_ARTIST"),
    ADMIN("ROLE_ADMIN");

    private final String value;
}
