package com.encore.encore.global.common;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter // JSON으로 변환되려면 Getter가 필수입니다!
public class CommonResponse<T> {
    private final boolean success;
    private final String message;
    private final T data;
    private final LocalDateTime timestamp;

    private CommonResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // 성공 시 이 메서드만 씁니다!
    public static <T> CommonResponse<T> ok(T data, String message) {
        return new CommonResponse<>(true, message, data);
    }
}

