package com.encore.encore.domain.user.controller;

import com.encore.encore.domain.user.dto.EmailVerifyRequestDto;
import com.encore.encore.domain.user.service.EmailService;
import com.encore.encore.global.common.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    /**
     * 사용자의 이메일로 6자리 인증 번호를 발송합니다.
     * @param emailVerifyRequestDto 인증 번호를 받을 이메일 주소
     * @return 성공 메시지 (CommonResponse)
     */
    @PostMapping("/send")
    public ResponseEntity<CommonResponse<Void>> sendEmail(
        @RequestBody EmailVerifyRequestDto emailVerifyRequestDto) {
        log.info("[이메일 발송 요청] Email: {}", emailVerifyRequestDto.getEmail());

        emailService.sendVerificationCode(emailVerifyRequestDto.getEmail());
        log.info("[이메일 발송 완료] Email: {}", emailVerifyRequestDto.getEmail());

        return ResponseEntity.ok(CommonResponse.ok(null, "인증 코드가 발송되었습니다."));
    }

    /**
     * 사용자가 입력한 인증 번호의 유효성을 검증합니다.
     * @param emailVerifyRequestDto
     * @return 인증 성공 메시지 (CommonResponse)
     */
    @PostMapping("/verify")
    public ResponseEntity<CommonResponse<Void>> verifyCode(
        @RequestBody EmailVerifyRequestDto emailVerifyRequestDto) {
        log.info("[이메일 인증 확인 요청] Email: {}", emailVerifyRequestDto.getEmail());

        emailService.verifyCode(emailVerifyRequestDto);
        log.info("[이메일 인증 성공] Email: {}", emailVerifyRequestDto.getEmail());

        return ResponseEntity.ok(CommonResponse.ok(null, "인증에 성공하였습니다."));
    }
}
