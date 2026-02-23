package com.encore.encore.domain.user.service;

import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.user.dto.UserNotificationDto;
import com.encore.encore.domain.user.entity.User;
import com.encore.encore.domain.user.entity.UserNotification;
import com.encore.encore.domain.user.repository.UserNotificationRepository;
import com.encore.encore.domain.user.repository.UserRepository;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserSettingService {

    private final UserNotificationRepository userNotificationRepository;
    private final UserRepository userRepository;

    /**
     * 로그인 + 모드 기준 알림 설정 정보를 가지고 온다.
     *
     * @param userId      로그인 되어있는 유저ID
     * @param profileMode 사용중인 프로필 모드
     * @return 알림 설정 정보
     */
    public UserNotificationDto getNotificationSettings(Long userId, ActiveMode profileMode) {
        UserNotification userNotification = userNotificationRepository
            .findByUser_UserIdAndProfileMode(userId, profileMode)
            .orElseGet(() -> createDefaultNotification(userId, profileMode));

        return UserNotificationDto.builder()
            .performanceStartAlert(userNotification.getPerformanceStartAlert())
            .dmAlert(userNotification.getDmAlert())
            .build();
    }

    /**
     * 로그인 + 모드 기준 알림 설정 업데이트
     *
     * @param userId      로그인 되어있는 유저ID
     * @param profileMode 사용중인 프로필 모드
     * @param dto         새롭게 설정할 알림 정보
     * @return 알림 설정된 정보
     */
    public UserNotificationDto updateNotificationSettings(Long userId, ActiveMode profileMode, UserNotificationDto dto) {
        UserNotification userNotification = userNotificationRepository
            .findByUser_UserIdAndProfileMode(userId, profileMode)
            .orElseGet(() -> createDefaultNotification(userId, profileMode));

        userNotification.setPerformanceStartAlert(dto.getPerformanceStartAlert());
        userNotification.setDmAlert(dto.getDmAlert());

        userNotificationRepository.save(userNotification);

        return UserNotificationDto.builder()
            .performanceStartAlert(userNotification.getPerformanceStartAlert())
            .dmAlert(userNotification.getDmAlert())
            .build();
    }

    /**
     * 새로운 알림 정보를 생성(최초시)
     *
     * @param userId      로그인 되어있는 유저ID
     * @param profileMode 선택되어있는 프로필모드
     * @return 새로 생성된 로그인 되어있는 유저의 알림 설정 정보
     */
    private UserNotification createDefaultNotification(Long userId, ActiveMode profileMode) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "유저 없음"));

        UserNotification notification = UserNotification.builder()
            .user(user)
            .profileMode(profileMode)
            .performanceStartAlert(true)
            .dmAlert(true)
            .build();

        return userNotificationRepository.save(notification);
    }
}
