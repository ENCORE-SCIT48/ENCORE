package com.encore.encore.domain.member.service;

import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.member.entity.HostProfile;
import com.encore.encore.domain.member.repository.HostProfileRepository;
import com.encore.encore.domain.member.repository.PerformerProfileRepository;
import com.encore.encore.domain.member.repository.UserProfileRepository;
import com.encore.encore.domain.user.entity.User;
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
    public Long findProfileIdByMode(ActiveMode mode, User user) {
        return switch (mode) {
            case PERFORMER -> performerProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("공연자 프로필이 없습니다."))
                .getPerformerId();
            case HOST -> hostProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("주최자 프로필이 없습니다."))
                .getHostId();
            case USER -> userProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("관람객 프로필이 없습니다."))
                .getProfileId();
        }; // 이 끝에 세미콜론(;)이 반드시 있어야 합니다!
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
     * @param profileMode 현재 사용자의 활성 모드 ({@link ActiveMode#USER}, {@link ActiveMode#PERFORMER}, {@link ActiveMode#HOST})
     * @return 모드에 따른 식별 명칭 (PERFORMER: 활동명, HOST: 단체명, USER: null).
     * 조회 결과가 없을 경우 "Unknown"을 반환합니다.
     */
    public String resolveSenderName(Long profileId, ActiveMode profileMode) {
        return switch (profileMode) {
            case USER -> userProfileRepository.findById(profileId)
                .map(p -> p.getUser().getNickname()) // 람다 사용
                .orElse("Unknown");
            case PERFORMER -> performerProfileRepository.findById(profileId)
                .map(p -> p.getStageName()) // 람다 사용
                .orElse("Unknown");
            case HOST -> hostProfileRepository.findById(profileId)
                .map(HostProfile::getOrganizationName)
                .orElse("Unknown");
        };
    }

}
