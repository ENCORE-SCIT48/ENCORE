package com.encore.encore.domain.member.service;

import com.encore.encore.domain.member.entity.ActiveMode;
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
     * @param mode 현재 선택된 사용자 모드 (USER, PERFORMER, HOST)
     * @param user 로그인한 사용자 엔티티
     * @return 해당 모드에 대응되는 프로필의 PK ID
     * @throws RuntimeException
     *         선택된 모드에 해당하는 프로필이 존재하지 않을 경우
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
}
