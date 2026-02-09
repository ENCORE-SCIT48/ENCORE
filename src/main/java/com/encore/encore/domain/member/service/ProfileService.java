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

    /**
     * 사용자의 활성 모드에 따라 해당 프로필의 PK ID를 조회합니다.
     * @param mode 현재 선택된 프로필
     * @param user 로그인한 사용자 객체
     * @return 프로필 PK ID
     * @throws ApiException 프로필이 존재하지 않을 경우 (ErrorCode.NOT_FOUND)
     */
    public Long findProfileIdByMode(ActiveMode mode, User user) {
        return switch (mode) {
            case USER -> userProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() ->
                    new ApiException(ErrorCode.NOT_FOUND, "해당 사용자의 관람객 프로필을 찾을 수 없습니다."))
                .getProfileId();

            case PERFORMER -> performerProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() ->
                    new ApiException(ErrorCode.NOT_FOUND, "해당 사용자의 공연자 프로필을 찾을 수 없습니다."))
                .getPerformerId();

            case HOST -> hostProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() ->
                    new ApiException(ErrorCode.NOT_FOUND, "해당 사용자의 주최자 프로필을 찾을 수 없습니다."))
                .getHostId();
        };
    }

    /**
     * [설명] 선택한 프로필의 초기 필수 정보 입력 완료 여부를 확인합니다.
     * @param mode      조회할 활성 모드 (USER, PERFORMER, HOST)
     * @param profileId 해당 모드의 프로필 PK ID
     * @return 초기화 완료 여부 (true: 완료, false: 미완료)
     * @throws ApiException 프로필 ID가 존재하지 않을 경우 (ErrorCode.NOT_FOUND)
     */
    public boolean checkIfInitialized(ActiveMode mode, Long profileId) {
        // 1. 방어 코드: ID가 null이면 미설정으로 간주
        if (profileId == null) {
            log.warn("[Profile Check] Profile ID is null for mode: {}", mode);
            return false;
        }

        // 2. 모드별 레포지토리 조회 및 초기화 상태 반환
        return switch (mode) {
            case USER -> userProfileRepository.findById(profileId)
                .map(UserProfile::isInitialized)
                .orElseThrow(() ->
                    new ApiException(ErrorCode.NOT_FOUND
                        , "관람객 프로필 정보를 찾을 수 없습니다. ID: " + profileId));

            case PERFORMER -> performerProfileRepository.findById(profileId)
                .map(PerformerProfile::isInitialized)
                .orElseThrow(() ->
                    new ApiException(ErrorCode.NOT_FOUND
                        , "공연자 프로필 정보를 찾을 수 없습니다. ID: " + profileId));

            case HOST -> hostProfileRepository.findById(profileId)
                .map(HostProfile::isInitialized)
                .orElseThrow(() ->
                    new ApiException(ErrorCode.NOT_FOUND
                        , "주최자 프로필 정보를 찾을 수 없습니다. ID: " + profileId));
        };
    }
}
