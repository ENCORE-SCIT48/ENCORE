package com.encore.encore.domain.member.service;

import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.member.entity.HostProfile;
import com.encore.encore.domain.member.entity.PerformerProfile;
import com.encore.encore.domain.member.entity.UserProfile;
import com.encore.encore.domain.member.repository.HostProfileRepository;
import com.encore.encore.domain.member.repository.PerformerProfileRepository;
import com.encore.encore.domain.member.repository.UserProfileRepository;
import com.encore.encore.domain.user.entity.User;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final UserProfileRepository userProfileRepository;
    private final PerformerProfileRepository performerProfileRepository;
    private final HostProfileRepository hostProfileRepository;

    public Long findProfileIdByMode(ActiveMode mode, User user) {
        // [중요] 어떤 유저가 무슨 모드를 찾으려 하는가 (흐름 추적용)
        log.info("[Profile Search] User: {}, Mode: {}", user.getUserId(), mode);

        return switch (mode) {
            case USER -> userProfileRepository.findByUser_UserId(user.getUserId())
                .map(UserProfile::getProfileId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "관람객 프로필 부재"));

            case PERFORMER -> performerProfileRepository.findByUser_UserId(user.getUserId())
                .map(PerformerProfile::getPerformerId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "공연자 프로필 부재"));

            case HOST -> hostProfileRepository.findByUser_UserId(user.getUserId())
                .map(HostProfile::getHostId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "주최자 프로필 부재"));
        };
    }

    public boolean checkIfInitialized(ActiveMode mode, Long profileId) {
        // [중요] ID가 없어 초기화가 불가능한 상황 (경고성 로그)
        if (profileId == null) {
            log.warn("[Profile Check] Null ID for Mode: {}", mode);
            return false;
        }

        boolean isInitialized = switch (mode) {
            case USER -> userProfileRepository.findById(profileId)
                .map(UserProfile::isInitialized)
                .orElse(false); // 가이드라인: 없으면 false 반환하여 등록 폼 유도

            case PERFORMER -> performerProfileRepository.findById(profileId)
                .map(PerformerProfile::isInitialized)
                .orElse(false);

            case HOST -> hostProfileRepository.findById(profileId)
                .map(HostProfile::isInitialized)
                .orElse(false);
        };

        // [중요] 최종 판정 결과 (리다이렉트 원인 파악용)
        if (!isInitialized) {
            log.info("[Profile Check] Mode: {} (ID: {}) is NOT initialized.", mode, profileId);
        }

        return isInitialized;
    }
}
