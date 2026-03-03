package com.encore.encore.global.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException e, HttpServletRequest request) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
            .status(errorCode.getStatus())
            .body(ErrorResponse.of(errorCode, request.getRequestURI(), e.getMessage()));
    }

    /**
     * @Valid @RequestBody DTO (예: 공연장 대관 신청/거절)의 검증 실패 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                      HttpServletRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::toFieldError)
            .collect(Collectors.toList());

        ErrorResponse body = ErrorResponse.builder()
            .timestamp(java.time.LocalDateTime.now())
            .status(ErrorCode.INVALID_REQUEST.getStatus().value())
            .code(ErrorCode.INVALID_REQUEST.getCode())
            .message("요청 값이 올바르지 않습니다.")
            .path(request.getRequestURI())
            .fieldErrors(fieldErrors)
            .build();

        return ResponseEntity
            .status(ErrorCode.INVALID_REQUEST.getStatus())
            .body(body);
    }

    /**
     * 쿼리 파라미터/폼 데이터 바인딩 오류 처리
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException e,
                                                             HttpServletRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::toFieldError)
            .collect(Collectors.toList());

        ErrorResponse body = ErrorResponse.builder()
            .timestamp(java.time.LocalDateTime.now())
            .status(ErrorCode.INVALID_REQUEST.getStatus().value())
            .code(ErrorCode.INVALID_REQUEST.getCode())
            .message("요청 파라미터가 올바르지 않습니다.")
            .path(request.getRequestURI())
            .fieldErrors(fieldErrors)
            .build();

        return ResponseEntity
            .status(ErrorCode.INVALID_REQUEST.getStatus())
            .body(body);
    }

    /**
     * @Validated 파라미터 등에서 발생하는 제약 조건 위반 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e,
                                                                   HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.builder()
            .timestamp(java.time.LocalDateTime.now())
            .status(ErrorCode.INVALID_REQUEST.getStatus().value())
            .code(ErrorCode.INVALID_REQUEST.getCode())
            .message(e.getMessage())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity
            .status(ErrorCode.INVALID_REQUEST.getStatus())
            .body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        e.printStackTrace();
        return ResponseEntity
            .status(ErrorCode.INTERNAL_ERROR.getStatus())
            .body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR, request.getRequestURI(), "서버 내부 오류가 발생했습니다."));
    }

    private ErrorResponse.FieldError toFieldError(FieldError fieldError) {
        Object rejected = fieldError.getRejectedValue();
        String value = rejected == null ? "" : rejected.toString();
        return new ErrorResponse.FieldError(fieldError.getField(), value, fieldError.getDefaultMessage());
    }
}
