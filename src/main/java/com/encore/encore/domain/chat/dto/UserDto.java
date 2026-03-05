package com.encore.encore.domain.chat.dto;

public record UserDto(
    Long profileId,
    String profileMode, // 예: "ACTIVE", "GUEST" 등
    String email,
    String username
) {
}
