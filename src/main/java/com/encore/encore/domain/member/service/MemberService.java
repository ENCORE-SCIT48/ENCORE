package com.encore.encore.domain.member.service;

import com.encore.encore.domain.member.dto.MemberProfileDto;
import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.member.entity.HostProfile;
import com.encore.encore.domain.member.entity.PerformerProfile;
import com.encore.encore.domain.member.entity.UserProfile;
import com.encore.encore.domain.member.repository.HostProfileRepository;
import com.encore.encore.domain.member.repository.PerformerProfileRepository;
import com.encore.encore.domain.member.repository.UserProfileRepository;
import com.encore.encore.domain.user.repository.UserRelationRepository;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final UserProfileRepository userProfileRepository;
    private final PerformerProfileRepository performerProfileRepository;
    private final HostProfileRepository hostProfileRepository;
    private final UserRelationRepository userRelationRepository;

    /**
     * 유저의 개인페이지에 출력할 정보를 불러옵니다.
     *
     * @param profileId   개인페이지 유저의 프로필 id
     * @param profileMode 개인페이지 유저의 프로필 모드
     * @return
     */
    public MemberProfileDto getMemberProfileInfo(Long profileId, String profileMode) {

        ActiveMode activeProfileMode = ActiveMode.valueOf(profileMode);


        switch (activeProfileMode) {
            case ROLE_USER:
                return getUserProfileDto(profileId, profileMode);

            case ROLE_PERFORMER:
                return getPerformerProfileDto(profileId, profileMode);

            case ROLE_HOST:
                return getHostProfileDto(profileId, profileMode);

            default:
                throw new ApiException(ErrorCode.INVALID_REQUEST, "지원하지 않는 프로필 모드입니다.");
        }
    }

    /**
     * 유저 프로필 DTO 생성
     */
    private MemberProfileDto getUserProfileDto(Long profileId, String profileMode) {
        UserProfile userProfile = userProfileRepository.findById(profileId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "유저 프로필을 찾을 수 없습니다."));

        // userInfo 포맷 예시: "소개 내용 (생년월일: YYYY-MM-DD)"
        String userInfo = String.format("%s (생년월일: %s)",
            userProfile.getIntroduction(),
            userProfile.getBirthDate() != null ? userProfile.getBirthDate().toString() : "미등록");

        int followingCount = userRelationRepository.countFollowing(userProfile.getUser().getUserId(), ActiveMode.valueOf(profileMode));

        int followerCount = userRelationRepository.countFollower(profileId, ActiveMode.valueOf(profileMode));

        String profileImg = userProfile.getProfileImageUrl();
        if (profileImg == null || profileImg.isBlank()) {
            profileImg = "/image/default-profile.png";
        }

        return MemberProfileDto.builder()
            .profileId(profileId)
            .profileMode(profileMode)
            .userName(userProfile.getUser().getNickname())
            .profileImg(profileImg)
            .userInfo(userInfo)
            .followerCount(followerCount)
            .followingCount(followingCount)
            .build();
    }

    /**
     * 공연자 프로필 DTO 생성
     */
    private MemberProfileDto getPerformerProfileDto(Long profileId, String profileMode) {
        PerformerProfile performerProfile = performerProfileRepository.findById(profileId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "공연자 프로필을 찾을 수 없습니다."));

        int followingCount = userRelationRepository.countFollowing(performerProfile.getUser().getUserId(), ActiveMode.valueOf(profileMode));

        int followerCount = userRelationRepository.countFollower(profileId, ActiveMode.valueOf(profileMode));

        return MemberProfileDto.builder()
            .profileId(profileId)
            .profileMode(profileMode)
            .userName(performerProfile.getStageName())
            .profileImg(null) // TODO: 프로필 이미지 엔티티 추가 시 변경
            .userInfo(performerProfile.getCategory())
            .followerCount(followerCount)
            .followingCount(followingCount)
            .build();
    }

    /**
     * 호스트 프로필 DTO 생성
     */
    private MemberProfileDto getHostProfileDto(Long profileId, String profileMode) {
        HostProfile hostProfile = hostProfileRepository.findById(profileId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "호스트 프로필을 찾을 수 없습니다."));

        int followingCount = userRelationRepository.countFollowing(hostProfile.getUser().getUserId(), ActiveMode.valueOf(profileMode));

        int followerCount = userRelationRepository.countFollower(profileId, ActiveMode.valueOf(profileMode));


        return MemberProfileDto.builder()
            .profileId(profileId)
            .profileMode(profileMode)
            .userName(hostProfile.getOrganizationName())
            .profileImg(null) // TODO: 프로필 이미지 엔티티 추가 시 변경
            .userInfo(hostProfile.getBusinessNumber())
            .followerCount(followerCount)
            .followingCount(followingCount)
            .build();
    }
}
