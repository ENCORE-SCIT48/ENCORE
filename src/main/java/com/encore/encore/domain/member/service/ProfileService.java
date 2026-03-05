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
     * 사용자의 활성 모드에 따라 해당 프로필의 PK ID를 조회한다.
     * ActiveMode 값에 따라 서로 다른 프로필 테이블을 조회하며,
     *
     * @param mode 현재 선택된 사용자 모드 (USER, PERFORMER, HOST)
     * @param user 로그인한 사용자 엔티티
     * @return 해당 모드에 대응되는 프로필의 PK ID
     * @throws RuntimeException 선택된 모드에 해당하는 프로필이 존재하지 않을 경우
     */
    @Transactional(readOnly = true)
    public Long findProfileIdByMode(ActiveMode mode, User user) {
        // [중요] 어떤 유저가 무슨 모드를 찾으려 하는가 (흐름 추적용)
        log.info("[Profile Search] User: {}, Mode: {}", user.getUserId(), mode);

        return switch (mode) {
            case ROLE_USER -> userProfileRepository.findByUser_UserId(user.getUserId())
                .map(UserProfile::getProfileId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "관람객 프로필 부재"));

            case ROLE_PERFORMER -> performerProfileRepository.findByUser_UserId(user.getUserId())
                .map(PerformerProfile::getPerformerId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "공연자 프로필 부재"));

            case ROLE_HOST -> hostProfileRepository.findByUser_UserId(user.getUserId())
                .map(HostProfile::getHostId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "주최자 프로필 부재"));
        };
    }

    /**
     * [설명] 선택한 모드(USER, PERFORMER, HOST)의 처음 접속 확인
     * 각 프로필 테이블의 isInitialized 컬럼을 조회하여,
     * 필수 정보 입력이 완료된 상태인지 판단합니다.
     *
     * @param mode      검사할 프로필 (USER, PERFORMER, HOST)
     * @param profileId 해당 모드의 프로필 PK ID
     * @return 초기화 완료 여부 (true: 완료, false: 미완료)
     */
    @Transactional(readOnly = true)
    public boolean checkIfInitialized(ActiveMode mode, Long profileId) {
        // [중요] ID가 없어 초기화가 불가능한 상황 (경고성 로그)
        if (profileId == null) {
            log.warn("[Profile Check] Null ID for Mode: {}", mode);
            return false;
        }

        boolean isInitialized = switch (mode) {
            case ROLE_USER -> userProfileRepository.findById(profileId)
                .map(UserProfile::isInitialized)
                .orElse(false); // 가이드라인: 없으면 false 반환하여 등록 폼 유도

            case ROLE_PERFORMER -> performerProfileRepository.findById(profileId)
                .map(PerformerProfile::isInitialized)
                .orElse(false);

            case ROLE_HOST -> hostProfileRepository.findById(profileId)
                .map(HostProfile::isInitialized)
                .orElse(false);
        };

        // [중요] 최종 판정 결과 (리다이렉트 원인 파악용)
        if (!isInitialized) {
            log.info("[Profile Check] Mode: {} (ID: {}) is NOT initialized.", mode, profileId);
        }

        return isInitialized;

    }

    /**
     * 참여자의 활성 모드에 따라 표시될 서비스용 닉네임(발신자 명칭)을 결정합니다.
     * <p>
     * 이 메소드는 각 프로필 타입별 특화된 이름(공연자 명칭, 단체명 등)을 조회하며,
     * 일반 사용자(USER)의 경우 별도의 특화 명칭이 없으므로 {@code null}을 반환합니다.
     * 반환된 {@code null}은 호출부에서 기본 닉네임 또는 익명 처리에 활용될 수 있습니다.
     * </p>
     *
     * @param profileId   조회하려는 모드에 해당하는 프로필 엔티티의 식별자 (PK)
     * @param profileMode 현재 사용자의 활성 모드 ({@link ActiveMode#ROLE_USER}, {@link ActiveMode#ROLE_PERFORMER}, {@link ActiveMode#ROLE_HOST})
     * @return 모드에 따른 식별 명칭 (PERFORMER: 활동명, HOST: 단체명, USER: null).
     * 조회 결과가 없을 경우 "Unknown"을 반환합니다.
     */
    public String resolveSenderName(Long profileId, ActiveMode profileMode) {
        return switch (profileMode) {
            case ROLE_USER -> userProfileRepository.findById(profileId)
                .map(p -> p.getUser().getNickname()) // 람다 사용
                .orElse("Unknown");
            case ROLE_PERFORMER -> performerProfileRepository.findById(profileId)
                .map(p -> p.getStageName()) // 람다 사용
                .orElse("Unknown");
            case ROLE_HOST -> hostProfileRepository.findById(profileId)
                .map(HostProfile::getOrganizationName)
                .orElse("Unknown");
        };
    }

    /**
     * 현재 활성 모드에 해당하는 프로필의 이미지 URL을 조회한다.
     * 헤더 프로필 아이콘에 표시할 때 사용.
     *
     * @param user 로그인한 사용자
     * @param mode 활성 모드 (ROLE_USER, ROLE_PERFORMER, ROLE_HOST)
     * @return 프로필 이미지 URL (없으면 null)
     */
    @Transactional(readOnly = true)
    public String getProfileImageUrl(User user, ActiveMode mode) {
        if (user == null || mode == null) return null;
        return switch (mode) {
            case ROLE_USER -> userProfileRepository.findByUser_UserId(user.getUserId())
                .map(UserProfile::getProfileImageUrl)
                .orElse(null);
            case ROLE_PERFORMER -> performerProfileRepository.findByUser_UserId(user.getUserId())
                .map(PerformerProfile::getProfileImageUrl)
                .orElse(null);
            case ROLE_HOST -> hostProfileRepository.findByUser_UserId(user.getUserId())
                .map(HostProfile::getProfileImageUrl)
                .orElse(null);
        };
    }
}
