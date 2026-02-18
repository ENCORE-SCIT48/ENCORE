package com.encore.encore.domain.member.entity;

/**
 * [설명] 공연자의 실력 등급을 정의하는 Enum입니다.
 */
public enum SkillLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED;

    public static SkillLevel from(String value) {
        if (value == null || value.isBlank()) return null;

        try {
            return SkillLevel.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid skillLevel: " + value);
        }
    }
}
