package com.encore.encore.domain.user.service;

import com.encore.encore.domain.member.entity.HostProfile;
import com.encore.encore.domain.member.entity.PerformerProfile;
import com.encore.encore.domain.member.entity.UserProfile;
import com.encore.encore.domain.member.repository.HostProfileRepository;
import com.encore.encore.domain.member.repository.PerformerProfileRepository;
import com.encore.encore.domain.member.repository.UserProfileRepository;
import com.encore.encore.domain.user.dto.UserJoinRequestDto;
import com.encore.encore.domain.user.entity.User;
import com.encore.encore.domain.user.entity.UserNotification;
import com.encore.encore.domain.user.entity.UserRole;
import com.encore.encore.domain.user.entity.UserStatus;
import com.encore.encore.domain.user.repository.EmailVerificationRepository;
import com.encore.encore.domain.user.repository.UserNotificationRepository;
import com.encore.encore.domain.user.repository.UserRepository;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationRepository emailVerificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final UserProfileRepository userProfileRepository;
    private final PerformerProfileRepository performerProfileRepository;
    private final HostProfileRepository hostProfileRepository;

    /**
     * 회원가입을 처리합니다.
     *
     * @param userJoinRequestDto 회원 정보  객체
     * @return user.getEmail() 회원 이메일
     */
    @Transactional
    public String join(UserJoinRequestDto userJoinRequestDto) {
        String email = userJoinRequestDto.getEmail();
        log.info("회원가입 프로세스 시작 - Email: {}", email);

        // 1. 필수 약관 검증 (Fail-Fast)
        if (!userJoinRequestDto.isAgreeTerms() || !userJoinRequestDto.isAgreePrivacy()) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "필수 약관에 동의해야 합니다.");
        }

        // 2. 비밀번호 일치 여부 확인
        if (!userJoinRequestDto.getPassword().equals(userJoinRequestDto.getPasswordConfirm())) {
            throw new ApiException(ErrorCode.INVALID_REQUEST);
        }

        // 3.가장 최근에 성공한 인증 기록이 있는지 확인합니다
        emailVerificationRepository.findValidVerification(email).orElseThrow(() -> {
            log.warn("[UserService.join] 이메일 미인증 - Email: {}", email); // 실패 원인을 명확히 기록
            return new ApiException(ErrorCode.INVALID_REQUEST, "이메일 인증이 필요합니다.");
        });
        log.info("[UserService.join] 이메일 인증 확인 완료 - Email: {}", email);

        //4. 중복 체크
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(ErrorCode.CONFLICT);
        }

        try {
            //5. 저장
            User user = User.builder().email(userJoinRequestDto.getEmail()).passwordHash(passwordEncoder.encode(userJoinRequestDto.getPassword())) // 암호화 필수!
                .nickname(userJoinRequestDto.getNickname()).role(UserRole.USER) // 기본 권한 설정
                .agreeTerms(userJoinRequestDto.isAgreeTerms()).agreePrivacy(userJoinRequestDto.isAgreePrivacy()).agreeMarketing(userJoinRequestDto.isAgreeMarketing()).agreedAt(LocalDateTime.now()).status(UserStatus.ACTIVE)  // 초기 상태
                .build();

            userRepository.save(user);
            log.info("회원가입 완료 - 성공적으로 DB에 저장됨: {}", email);

            // 6. 알림 설정 자동 생성
            UserNotification notification = UserNotification.builder().user(user).performanceStartAlert(true) // 기본값 ON
                .dmAlert(true)               // 기본값 ON
                .build();

            userNotificationRepository.save(notification);

            // 7. 프로필 생성
            userProfileRepository.save(UserProfile.builder().user(user).build());
            performerProfileRepository.save(PerformerProfile.builder().user(user).build()); // 활동명은 일단 닉네임으로!
            hostProfileRepository.save(HostProfile.builder().user(user).build());

            return user.getEmail();
        } catch (Exception e) {

            log.error("회원가입 중 서버 에러 발생 - Email: {}, Error: {}", email, e.getMessage(), e);

            // 예외를 그대로 던져서 GlobalExceptionHandler가 CommonResponse 규격으로 변환하게 함
            throw e;
        }
    }

}
