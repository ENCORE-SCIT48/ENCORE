package com.encore.encore.domain.user.service;

import com.encore.encore.domain.user.dto.UserJoinRequestDto;
import com.encore.encore.domain.user.entity.User;
import com.encore.encore.domain.user.entity.UserRole;
import com.encore.encore.domain.user.entity.UserStatus;
import com.encore.encore.domain.user.repository.UserRepository;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * [설명] 회원가입을 처리합니다.
     * @param userJoinRequestDto 회원 정보  객체
     * @return user.getEmail() 회원 이메일
     */
    public String join(UserJoinRequestDto userJoinRequestDto) {
        // 1. 비밀번호 일치 여부 확인
        if (!userJoinRequestDto.getPassword().equals(userJoinRequestDto.getPasswordConfirm())) {
            // 일치하지 않으면 예외 발생 (기존 ErrorCode.INVALID_REQUEST 활용)
            throw new ApiException(ErrorCode.INVALID_REQUEST);
        }

        //2. 중복 체크
        if (userRepository.existsByEmail(userJoinRequestDto.getEmail())) {
            throw new ApiException(ErrorCode.CONFLICT);
        }
        //3. 저장
        User user = User.builder().email(userJoinRequestDto.getEmail()).passwordHash(passwordEncoder.encode(userJoinRequestDto.getPassword())) // 암호화 필수!
            .nickname(userJoinRequestDto.getNickname()).role(UserRole.USER) // 기본 권한 설정
            .status(UserStatus.ACTIVE)  // 초기 상태
            .build();

        userRepository.save(user);


        //4. 리턴
        return user.getEmail();
    }

}
