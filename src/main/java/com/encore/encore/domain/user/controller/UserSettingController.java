package com.encore.encore.domain.user.controller;

import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.user.dto.UserNotificationDto;
import com.encore.encore.domain.user.service.UserSettingService;
import com.encore.encore.global.common.CommonResponse;
import com.encore.encore.global.config.CustomUserDetails;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/users/settings")
public class UserSettingController {

    private final UserSettingService userSettingService;

    /**
     * 유저의 알림 설정 정보를 가져온다.
     */
    @GetMapping("/notifications")
    public ResponseEntity<CommonResponse<UserNotificationDto>> getNotifications(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        Long userId = userDetails.getUser().getUserId();
        ActiveMode profileMode = userDetails.getActiveMode();

        UserNotificationDto result = userSettingService.getNotificationSettings(userId, profileMode);
        return ResponseEntity.ok(CommonResponse.ok(result, "알림 설정 조회 완료"));
    }

    /**
     * 유저의 알림 정보를 새롭게 설정한다.
     */
    @PutMapping("/notifications")
    public ResponseEntity<CommonResponse<UserNotificationDto>> updateNotifications(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody UserNotificationDto dto
    ) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        Long userId = userDetails.getUser().getUserId();
        ActiveMode profileMode = userDetails.getActiveMode();

        UserNotificationDto result = userSettingService.updateNotificationSettings(userId, profileMode, dto);
        return ResponseEntity.ok(CommonResponse.ok(result, "알림 설정 완료"));
    }
}
