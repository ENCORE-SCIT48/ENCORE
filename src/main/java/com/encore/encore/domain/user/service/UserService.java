package com.encore.encore.domain.user.service;

import com.encore.encore.domain.member.entity.HostProfile;
import com.encore.encore.domain.member.entity.PerformerProfile;
import com.encore.encore.domain.member.entity.UserProfile;
import com.encore.encore.domain.member.repository.HostProfileRepository;
import com.encore.encore.domain.member.repository.PerformerProfileRepository;
import com.encore.encore.domain.member.repository.UserProfileRepository;
import com.encore.encore.domain.user.dto.UserAccountUpdateRequestDto;
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
            throw new ApiException(ErrorCode.INVALID_REQUEST,"비밀번호가 일치하지 않습니다.");
        }

        // 3. 가장 최근에 성공한 인증 기록이 있는지 확인합니다 (derived query 사용)
        emailVerificationRepository.findFirstByEmailAndVerifiedTrueAndIsDeletedFalseOrderByCreatedAtDesc(email).orElseThrow(() -> {
            log.warn("[UserService.join] 이메일 미인증 - Email: {}", email); // 실패 원인을 명확히 기록
            return new ApiException(ErrorCode.INVALID_REQUEST, "이메일 인증이 필요합니다.");
        });
        log.info("[UserService.join] 이메일 인증 확인 완료 - Email: {}", email);

        //4. 중복 체크
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(ErrorCode.CONFLICT, "이미 가입된 이메일입니다.");
        }

        try {
            //5. 저장
            User user = User.builder().email(userJoinRequestDto.getEmail()).passwordHash(passwordEncoder.encode(userJoinRequestDto.getPassword())) // 암호화 필수!
                .nickname(userJoinRequestDto.getNickname()).role(UserRole.USER) // 기본 권한 설정
                .agreeTerms(userJoinRequestDto.isAgreeTerms()).agreePrivacy(userJoinRequestDto.isAgreePrivacy()).agreeMarketing(userJoinRequestDto.isAgreeMarketing()).agreedAt(LocalDateTime.now()).status(UserStatus.ACTIVE)  // 초기 상태
                .build();

            userRepository.save(user);
            log.info("회원가입 완료 - 성공적으로 DB에 저장됨: {}", email);

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

    /**
     * 회원정보(닉네임, 비밀번호) 수정.
     * 비밀번호 변경 시 currentPassword 검증 후 newPassword/newPasswordConfirm 일치 시에만 변경.
     *
     * @param userId 로그인한 유저 ID
     * @param dto    닉네임, 현재 비밀번호(선택), 새 비밀번호(선택), 새 비밀번호 확인
     */
    @Transactional
    public void updateAccount(Long userId, UserAccountUpdateRequestDto dto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));

        boolean wantPasswordChange = dto.getNewPassword() != null && !dto.getNewPassword().isBlank();

        if (wantPasswordChange) {
            if (dto.getCurrentPassword() == null || dto.getCurrentPassword().isBlank()) {
                throw new ApiException(ErrorCode.INVALID_REQUEST, "비밀번호 변경 시 현재 비밀번호를 입력해주세요.");
            }
            if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
                throw new ApiException(ErrorCode.INVALID_REQUEST, "현재 비밀번호가 일치하지 않습니다.");
            }
            if (!dto.getNewPassword().equals(dto.getNewPasswordConfirm())) {
                throw new ApiException(ErrorCode.INVALID_REQUEST, "새 비밀번호가 일치하지 않습니다.");
            }
            if (dto.getNewPassword().length() < 8) {
                throw new ApiException(ErrorCode.INVALID_REQUEST, "비밀번호는 8자 이상이어야 합니다.");
            }
            user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        }

        if (dto.getNickname() != null && !dto.getNickname().isBlank()) {
            user.setNickname(dto.getNickname().trim());
        }

        userRepository.save(user);
        log.info("[UserService.updateAccount] 완료 - userId={}", userId);
    }
}

