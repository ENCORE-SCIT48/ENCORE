package com.encore.encore.global.error;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ErrorResponse {
    private final LocalDateTime timestamp;
    private final int status;
    private final String code;
    private final String message;
    private final String path;
    private final List<FieldError> fieldErrors;

    public static ErrorResponse of(ErrorCode errorCode, String path, String message) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(message)
                .path(path)
                .build();
    }

    @Getter
    public static class FieldError {
        private String field;
        private String value;
        private String reason;
    }
}