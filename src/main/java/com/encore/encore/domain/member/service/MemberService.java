package com.encore.encore.domain.member.service;

import com.encore.encore.domain.member.dto.MemberProfileDto;
import com.encore.encore.domain.member.dto.RecentActivitiesDto;
import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.member.entity.HostProfile;
import com.encore.encore.domain.member.entity.PerformerProfile;
import com.encore.encore.domain.member.entity.UserProfile;
import com.encore.encore.domain.member.repository.HostProfileRepository;
import com.encore.encore.domain.member.repository.PerformerProfileRepository;
import com.encore.encore.domain.member.repository.UserPerformanceRelationInfoRepository;
import com.encore.encore.domain.member.repository.UserProfileRepository;
import com.encore.encore.domain.performance.entity.UserPerformanceRelation;
import com.encore.encore.domain.user.entity.User;
import com.encore.encore.domain.user.repository.UserRelationRepository;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final UserProfileRepository userProfileRepository;
    private final PerformerProfileRepository performerProfileRepository;
    private final HostProfileRepository hostProfileRepository;
    private final UserRelationRepository userRelationRepository;
    private final UserPerformanceRelationInfoRepository userPerformanceRelationInfoRepository;

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
        String introduction = Optional.ofNullable(userProfile.getIntroduction()).orElse("");

        String preferredGenres = Optional.ofNullable(userProfile.getPreferredGenres()).orElse("");
        String preferredPerformanceTypes = Optional.ofNullable(userProfile.getPreferredPerformanceTypes()).orElse("");

        String userInfo = String.format("%s%s%s",
            introduction.isEmpty() ? "" : introduction + "\n\n", // introduction이 있으면 줄바꿈 포함
            preferredGenres.isEmpty() ? "" : "선호하는 장르: " + preferredGenres + "\n",
            preferredPerformanceTypes.isEmpty() ? "" : "선호하는 공연 타입: " + preferredPerformanceTypes
        );

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

        // 소개 정보 가져옴
        String description = Optional.ofNullable(performerProfile.getDescription()).orElse("");
        String category = Optional.ofNullable(performerProfile.getCategory()).orElse("");
        String activityArea = Optional.ofNullable(performerProfile.getActivityArea()).orElse("");
        String part = Optional.ofNullable(performerProfile.getPart()).orElse("");
        String skil = Optional.ofNullable(performerProfile.getSkillLevel().name()).orElse("");

        // null-safe + 불필요한 줄바꿈 제거
        StringBuilder userInfoBuilder = new StringBuilder();

        if (!description.isEmpty()) {
            userInfoBuilder.append(description).append("\n\n");
        }
        if (!category.isEmpty()) {
            userInfoBuilder.append("장르: ").append(category).append("\n");
        }
        if (!activityArea.isEmpty()) {
            userInfoBuilder.append("주 활동 지역: ").append(activityArea).append("\n");
        }
        if (!part.isEmpty()) {
            userInfoBuilder.append("담당 파트: ").append(part).append("\n");
        }
        if (!skil.isEmpty()) {
            userInfoBuilder.append("스킬 : ").append(skil);
        }

        // 최종 문자열
        String userInfo = userInfoBuilder.toString();

        String profileImg = performerProfile.getProfileImageUrl();
        if (profileImg == null || profileImg.isBlank()) {
            profileImg = "/image/default-profile.png";
        }
        return MemberProfileDto.builder()
            .profileId(profileId)
            .profileMode(profileMode)
            .userName(performerProfile.getStageName())
            .profileImg(profileImg)
            .userInfo(userInfo)
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

        String profileImg = hostProfile.getProfileImageUrl();
        if (profileImg == null || profileImg.isBlank()) {
            profileImg = "/image/default-profile.png";
        }

        String orgName = Optional.ofNullable(hostProfile.getOrganizationName()).orElse("");
        String repName = Optional.ofNullable(hostProfile.getRepresentativeName()).orElse("");
        String bizNum = Optional.ofNullable(hostProfile.getBusinessNumber()).orElse("");
        String verifiedText = hostProfile.isVerified() ? "인증 완료" : "인증 필요";

        // null-safe + 불필요한 줄바꿈 제거
        StringBuilder userInfoBuilder = new StringBuilder();

        if (!orgName.isEmpty()) {
            userInfoBuilder.append(orgName).append("\n");
        }
        if (!repName.isEmpty()) {
            userInfoBuilder.append("대표: ").append(repName).append("\n");
        }
        if (!bizNum.isEmpty()) {
            userInfoBuilder.append("사업자번호: ").append(bizNum).append(" ");
        }

        // 마지막은 인증 상태
        userInfoBuilder.append(verifiedText);

        // 최종 문자열
        String userInfo = userInfoBuilder.toString();

        return MemberProfileDto.builder()
            .profileId(profileId)
            .profileMode(profileMode)
            .userName(hostProfile.getOrganizationName())
            .profileImg(profileImg)
            .userInfo(userInfo)
            .followerCount(followerCount)
            .followingCount(followingCount)
            .build();
    }

    /**
     * 프로필 id와 모드로 user객체를 가져온다
     *
     * @param profileId
     * @param profileMode
     * @return
     */
    public User getUser(Long profileId, String profileMode) {

        ActiveMode activeProfileMode = ActiveMode.valueOf(profileMode);


        switch (activeProfileMode) {
            case ROLE_USER:
                return userProfileRepository.findById(profileId).get().getUser();

            case ROLE_PERFORMER:
                return performerProfileRepository.findById(profileId).get().getUser();

            case ROLE_HOST:
                return hostProfileRepository.findById(profileId).get().getUser();

            default:
                throw new ApiException(ErrorCode.INVALID_REQUEST, "지원하지 않는 프로필 모드입니다.");
        }
    }

    /**
     * profileId, profileMode의 관람 정보를 가져온다
     *
     * @param profileId
     * @param profileMode
     * @return
     */
    public List<RecentActivitiesDto> getRecentActivities(Long profileId, String profileMode) {

        User user = getUser(profileId, profileMode);
        List<UserPerformanceRelation> userPerformanceRelationList = userPerformanceRelationInfoRepository.findTop5ByUser_UserIdOrderByCreatedAtDesc(user.getUserId());

        List<RecentActivitiesDto> recentActivitiesDtoList = new ArrayList<>();
        for (UserPerformanceRelation performanceRelation : userPerformanceRelationList) {
            RecentActivitiesDto dto = RecentActivitiesDto.builder()
                .performanceTitle(performanceRelation.getPerformance().getTitle()).build();

            recentActivitiesDtoList.add(dto);
        }

        return recentActivitiesDtoList;

    }

    /**
     * userId로 기본 USER 프로필의 profileId 조회
     */
    public Long getDefaultProfileIdByUserId(Long userId) {

        UserProfile userProfile = userProfileRepository.findByUser_UserId(userId)
            .orElseThrow(() -> new ApiException(
                ErrorCode.NOT_FOUND,
                "유저 프로필을 찾을 수 없습니다. userId=" + userId
            ));

        return userProfile.getProfileId();
    }
}
