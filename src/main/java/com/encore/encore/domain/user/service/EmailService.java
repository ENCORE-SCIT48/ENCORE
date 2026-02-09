package com.encore.encore.domain.user.service;

import com.encore.encore.domain.user.dto.EmailVerifyRequestDto;
import com.encore.encore.domain.user.entity.EmailVerification;
import com.encore.encore.domain.user.repository.EmailVerificationRepository;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * [설명] 이메일 인증 코드 발송 및 검증을 담당하는 서비스입니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final JavaMailSender mailSender;

    /**
     * 인증 코드를 생성하여 DB에 저장하고 실제 이메일을 발송합니다.
     * @param email 인증 코드를 받을 사용자의 이메일
     */
    @Transactional
    public void sendVerificationCode(String email) {
        log.info("[EmailService.sendVerificationCode] 발송 프로세스 시작 - Email: {}", email);
        // 1. 6자리 난수 생성
        String code = String.format("%06d", new Random().nextInt(1000000));
        // 2. 만료 시간 설정 (5분)
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(5);

        // 3. 기존 인증 정보 삭제 (가이드라인: 데이터 무결성 및 깔끔한 관리)
        emailVerificationRepository.deleteByEmail(email);
        log.debug("[EmailService.sendVerificationCode] 기존 인증 정보 정리 완료 - Email: {}", email);

        // 4. 엔티티 생성 및 저장 (본인 엔티티 구조 사용)
        EmailVerification verification = EmailVerification.builder()
            .email(email)
            .code(code)
            .expiredAt(expiredAt)
            .verified(false)
            .build();

        emailVerificationRepository.save(verification);
        log.info("인증 코드 DB 저장 완료 - Email: {}, Code: {}", email, code);

        // 5. 실제 이메일 발송 로직 추가
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("[Encore] 회원가입 인증 코드입니다.");
            message.setText("인증 코드 [ " + code + " ]를 입력해주세요. (5분 이내)");

            mailSender.send(message);
            log.info("이메일 발송 성공 - Email: {}", email);

        } catch (Exception e) {
            // [가이드라인 4-③] 장애 상황 기록
            log.error("이메일 발송 실패 - Email: {}, Error: {}", email, e.getMessage(), e);
            throw new ApiException(ErrorCode.INTERNAL_ERROR); // 글로벌 핸들러가 처리
        }
    }

    /**
     * 사용자가 입력한 인증 번호를 검증합니다.
     * @param emailVerifyRequestDto 이메일과 검증코드 정보
     */
    @Transactional
    public void verifyCode(EmailVerifyRequestDto  emailVerifyRequestDto) {
        log.info("[EmailService.verifyCode] 인증 검증 시작 - Email: {}", emailVerifyRequestDto.getEmail());
        // 1. 이메일과 코드로 인증 정보 조회
        EmailVerification verification = emailVerificationRepository
            .findFirstByEmailAndCodeAndIsDeletedFalseOrderByCreatedAtDesc(
                emailVerifyRequestDto.getEmail()
                ,emailVerifyRequestDto.getCode())
            .orElseThrow(() -> new ApiException(ErrorCode.INVALID_REQUEST, "인증 번호가 일치하지 않습니다."));

        // 2. 만료 시간 체크
        if (verification.isExpired()) {
            log.warn("인증 실패 - 시간 만료: {}", emailVerifyRequestDto.getEmail());
            throw new ApiException(ErrorCode.INVALID_REQUEST, "인증 시간이 만료되었습니다.");
        }

        // 3. 인증 성공 처리 (엔티티의 메서드 활용)
        verification.setVerified(true);
        log.info("인증 성공 - Email: {}", emailVerifyRequestDto.getEmail());
    }
}
