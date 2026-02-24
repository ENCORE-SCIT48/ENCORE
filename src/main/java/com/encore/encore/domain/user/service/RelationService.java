package com.encore.encore.domain.user.service;

import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.member.entity.HostProfile;
import com.encore.encore.domain.member.entity.PerformerProfile;
import com.encore.encore.domain.member.entity.UserProfile;
import com.encore.encore.domain.member.repository.HostProfileRepository;
import com.encore.encore.domain.member.repository.PerformerProfileRepository;
import com.encore.encore.domain.member.repository.UserProfileRepository;
import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.domain.performance.repository.PerformanceRepository;
import com.encore.encore.domain.user.dto.*;
import com.encore.encore.domain.user.entity.RelationType;
import com.encore.encore.domain.user.entity.TargetType;
import com.encore.encore.domain.user.entity.User;
import com.encore.encore.domain.user.entity.UserRelation;
import com.encore.encore.domain.user.repository.UserRelationRepository;
import com.encore.encore.domain.user.repository.UserRepository;
import com.encore.encore.domain.venue.entity.Venue;
import com.encore.encore.domain.venue.repository.VenueRepository;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class RelationService {

    private final UserRelationRepository userRelationRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PerformerProfileRepository performerProfileRepository;
    private final HostProfileRepository hostProfileRepository;
    private final PerformanceRepository performanceRepository;
    private final VenueRepository venueRepository;

    /**
     * 프로필 id, 프로필 모드로 찾을 각각의 정보를 찾을 경우(ProfileInfoDto반환)
     * <p>
     * 주어진 프로필 ID와 프로필 모드에 따라 해당 프로필의 엔티티를 조회하고, ProfileInfoDto에 담아 return 합니다.
     *
     * <p>프로필 모드는 USER, PERFORMER, HOST 중 하나여야 하며,
     * 각 모드에 따라 적절한 Repository에서 조회합니다.</p>
     *
     * @param profileId   조회할 프로필의 ID
     * @param profileMode 조회할 프로필의 모드 (USER, PERFORMER, HOST)
     * @return 프로필에 연결된 User 엔티티
     * @throws ApiException 프로필이 존재하지 않거나 알 수 없는 프로필 모드인 경우 발생
     *                      - USER 모드인데 프로필이 없으면 "유저 프로필이 존재하지 않습니다."
     *                      - PERFORMER 모드인데 프로필이 없으면 "공연자 프로필이 존재하지 않습니다."
     *                      - HOST 모드인데 프로필이 없으면 "호스트 프로필이 존재하지 않습니다."
     *                      - 알 수 없는 프로필 모드면 "존재하지 않는 프로필 모드입니다."
     */
    public ProfileInfoDto getProfileInfo(Long profileId, ActiveMode profileMode) {
        switch (profileMode) {
            case USER:
                UserProfile userProfile = userProfileRepository.findById(profileId)
                    .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "유저 프로필이 존재하지 않습니다."));
                return new ProfileInfoDto(userProfile.getProfileId(), profileMode.name(), userProfile.getUser().getNickname(), userProfile.getProfileImageUrl());
            case PERFORMER:
                PerformerProfile performerProfile = performerProfileRepository.findById(profileId)
                    .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "공연자 프로필이 존재하지 않습니다."));
                return new ProfileInfoDto(performerProfile.getPerformerId(), profileMode.name(), performerProfile.getStageName(), null);
            case HOST:
                HostProfile hostProfile = hostProfileRepository.findById(profileId)
                    .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "호스트 프로필이 존재하지 않습니다."));
                return new ProfileInfoDto(hostProfile.getHostId(), profileMode.name(), hostProfile.getOrganizationName(), null);
            default:
                throw new ApiException(ErrorCode.NOT_FOUND, "존재하지 않는 프로필 모드 입니다: " + profileMode);
        }
    }

    /**
     * 유저 Id, 프로필 모드로 각각 정보를 찾을 경우(ProfileInfoDto반환)
     * 유저Id를 기준으로 각 프로필 mode에 따라 조회합니다.
     *
     * @param userId      user의 아이디
     * @param profileMode 프로필 모드
     * @return 각 정보가 들어간 ProfileInfoDto
     */

    public ProfileInfoDto getProfileInfoUserId(Long userId, ActiveMode profileMode) {
        switch (profileMode) {
            case USER:
                UserProfile userProfile = userProfileRepository.findByUser_UserId(userId)
                    .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "유저 프로필이 존재하지 않습니다."));
                return new ProfileInfoDto(userProfile.getProfileId(), profileMode.name(), userProfile.getUser().getNickname(), userProfile.getProfileImageUrl());
            case PERFORMER:
                PerformerProfile performerProfile = performerProfileRepository.findByUser_UserId(userId)
                    .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "공연자 프로필이 존재하지 않습니다."));
                return new ProfileInfoDto(performerProfile.getPerformerId(), profileMode.name(), performerProfile.getStageName(), null);
            case HOST:
                HostProfile hostProfile = hostProfileRepository.findByUser_UserId(userId)
                    .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "호스트 프로필이 존재하지 않습니다."));
                return new ProfileInfoDto(hostProfile.getHostId(), profileMode.name(), hostProfile.getOrganizationName(), null);
            default:
                throw new ApiException(ErrorCode.NOT_FOUND, "존재하지 않는 프로필 모드 입니다: " + profileMode);
        }
    }

    /**
     * 프로필 id, 프로필 모드로 찾을 각각의 정보를 찾을 경우(User반환)
     *
     * @param profileId
     * @param profileMode
     * @return User엔티티
     */
    public User findProfileById(Long profileId, ActiveMode profileMode) {
        switch (profileMode) {
            case USER:
                UserProfile userProfile = userProfileRepository.findById(profileId)
                    .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "유저 프로필이 존재하지 않습니다."));
                return userProfile.getUser();
            case PERFORMER:
                PerformerProfile performerProfile = performerProfileRepository.findById(profileId)
                    .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "공연자 프로필이 존재하지 않습니다."));
                return performerProfile.getUser();
            case HOST:
                HostProfile hostProfile = hostProfileRepository.findById(profileId)
                    .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "호스트 프로필이 존재하지 않습니다."));
                return hostProfile.getUser();
            default:
                throw new ApiException(ErrorCode.NOT_FOUND, "존재하지 않는 프로필 모드 입니다." + profileMode);
        }
    }

    /**
     * 프로필 ID와 프로필 모드를 기반으로 해당 User의 ID를 조회합니다.
     *
     * @param profileId   프로필 ID
     * @param profileMode 프로필 모드
     * @return 해당 프로필에 연결된 User ID
     * @throws ApiException 프로필이 존재하지 않을 경우
     */
    public Long getUserIdByProfile(Long profileId, ActiveMode profileMode) {
        if (profileId == null || profileMode == null) return null;

        return switch (profileMode) {
            case USER -> userProfileRepository.findById(profileId)
                .map(UserProfile::getUser)
                .map(User::getUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "유저가 존재하지 않습니다."));
            case PERFORMER -> performerProfileRepository.findById(profileId)
                .map(PerformerProfile::getUser)
                .map(User::getUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "공연자가 존재하지 않습니다."));
            case HOST -> hostProfileRepository.findById(profileId)
                .map(HostProfile::getUser)
                .map(User::getUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "호스트가 존재하지 않습니다."));
            default -> throw new ApiException(ErrorCode.NOT_FOUND, "존재하지 않는 프로필 모드입니다: " + profileMode);

        };
    }

    /**
     * 특정 프로필이 다른 프로필을 팔로우하거나 언팔로우하도록 처리합니다.
     *
     * <p>
     * 1. 요청받은 actor(profileId, profileMode)와 target(targetProfileId, targetProfileMode)의
     * 기존 팔로우 관계(UserRelation)를 조회합니다.
     * 2. 관계가 존재하면:
     * - isDeleted가 true면 팔로우 상태로 복구 (restore)
     * - isDeleted가 false면 언팔로우 처리 (delete)
     * 3. 관계가 존재하지 않으면 새롭게 UserRelation을 생성하여 저장합니다.
     * </p>
     *
     * @param profileId         액터 프로필의 ID
     * @param profileMode       액터 프로필 모드 (USER, PERFORMER, HOST)
     * @param targetProfileId   팔로우/언팔로우 대상 프로필의 ID
     * @param targetProfileMode 팔로우/언팔로우 대상 프로필 모드 (USER, PERFORMER, HOST)
     * @return 팔로우 상태가 갱신된 결과를 담은 {@link ResponseFollowDto}
     * - targetId: 팔로우 대상 프로필 ID
     * - targetProfileMode: 팔로우 대상 프로필 모드
     * - isFollowing: 현재 팔로우 상태 (true: 팔로우중, false: 언팔로우)
     * @throws ApiException targetProfileMode 값이 올바르지 않거나, 조회 대상 프로필이 존재하지 않을 경우 발생
     */
    public ResponseFollowDto userFollow(Long profileId, ActiveMode profileMode, Long targetProfileId, String targetProfileMode) {

        // 1. 나(Actor)의 '알맹이(User)' 찾기
        User actor = findProfileById(profileId, profileMode);
        ActiveMode targetMode = ActiveMode.valueOf(targetProfileMode);


        // 2. [조회] 이전에 맺은 관계가 있는지 확인 (is_deleted 상관없이 다 뒤짐)
        // 여기서 actor.getUserId()를 쓰는 게 핵심입니다!
        Optional<UserRelation> optionalRelation = userRelationRepository
            .findExistingRelation(actor.getUserId(), profileMode, targetProfileId, targetMode, RelationType.FOLLOW);

        UserRelation relation;

        if (optionalRelation.isPresent()) {
            // 3-1. [수정] 관계가 이미 있다면? 상태만 반대로 뒤집기 (토글)
            relation = optionalRelation.get();
            if (relation.isDeleted()) {
                relation.restore(); // true -> false (다시 팔로우)
            } else {
                relation.delete();  // false -> true (언팔로우)
            }
            log.info("기존 관계 상태 변경: isDeleted={}", relation.isDeleted());
        } else {
            // 3-2. [생성] 관계가 아예 없다면? 새로 만들기
            relation = UserRelation.builder()
                .actor(actor) // User 객체 통째로 넣기
                .actorProfileMode(profileMode)
                .targetId(targetProfileId) // Target은 숫자 ID만
                .targetProfileMode(targetMode)
                .targetType(TargetType.USER)
                .relationType(RelationType.FOLLOW)
                .build();
            userRelationRepository.save(relation);
            log.info("새로운 관계 생성");
        }

        int followerCount = userRelationRepository.countFollower(targetProfileId, ActiveMode.valueOf(targetProfileMode));
        // 4. 결과 반환 (현재 팔로우 중인지 여부)
        return ResponseFollowDto.builder()
            .targetId(targetProfileId)
            .targetProfileMode(targetProfileMode)
            .isFollowing(!relation.isDeleted())
            .followerCount(followerCount)
            .build();
    }


    /**
     * 특정 프로필이 팔로우하고 있는 대상 목록(팔로잉 리스트)을 조회합니다.
     *
     * <p>
     * 동작 방식:
     * <ol>
     *   <li>targetId와 targetMode를 기반으로 해당 프로필의 실제 User ID를 조회합니다.</li>
     *   <li>해당 사용자가 FOLLOW 관계를 맺고 있는 모든 UserRelation을 조회합니다
     *       (isDeleted = false 인 관계만 조회).</li>
     *   <li>각 관계의 targetType(USER, PERFORMANCE, VENUE)에 따라
     *       적절한 엔티티를 조회하여 {@link ProfileInfoDto}로 변환합니다.</li>
     *   <li>로그인 사용자가 각 대상에 대해 팔로우 중인지 여부를 확인합니다.</li>
     *   <li>각 항목을 {@link ResponseFollowListDto}로 변환하여 반환합니다.</li>
     * </ol>
     * </p>
     *
     * <p>
     * targetType 처리 방식:
     * <ul>
     *   <li>USER → getProfileInfo()를 통해 프로필 정보 조회</li>
     *   <li>PERFORMANCE → performanceRepository를 통해 공연 정보 조회</li>
     *   <li>VENUE → venueRepository를 통해 공연장 정보 조회</li>
     * </ul>
     * </p>
     *
     * @param targetId         팔로잉 목록을 조회할 프로필 ID
     * @param targetMode       조회 대상 프로필의 활성 모드 (USER, PERFORMER, HOST 등)
     * @param loginProfileId   로그인한 사용자의 프로필 ID (팔로우 여부 확인용)
     * @param loginProfileMode 로그인한 사용자의 활성 모드
     * @return 로그인 사용자 기준 팔로우 여부(isFollowing)를 포함한
     * {@link ResponseFollowListDto} 리스트
     * @throws ApiException <ul>
     * @implNote 현재 구현은 각 relation마다 추가 조회가 발생할 수 있어
     * 팔로잉 수가 많을 경우 N+1 쿼리 문제가 발생할 수 있습니다.
     * 필요 시 fetch join 또는 일괄 조회 방식으로 최적화가 필요합니다.
     */
    public List<ResponseFollowListDto> getFollowingList(Long targetId, ActiveMode targetMode, Long loginProfileId, ActiveMode loginProfileMode) {

        Long targetUserId = getUserIdByProfile(targetId, targetMode);

        // 1️⃣ target 프로필이 팔로우하고 있는 관계 조회
        List<UserRelation> relations =
            userRelationRepository
                .findByActor_UserIdAndActorProfileModeAndRelationTypeAndIsDeletedFalse(
                    targetUserId,
                    targetMode,
                    RelationType.FOLLOW
                );

        // 2️⃣ DTO 변환
        List<ResponseFollowListDto> result = relations.stream()
            .map(relation -> {
                // targetType에 따라 조회
                ProfileInfoDto targetUser = switch (relation.getTargetType()) {
                    case USER -> getProfileInfo(relation.getTargetId(), relation.getTargetProfileMode());
                    case PERFORMANCE -> {
                        Performance performance = performanceRepository.findById(relation.getTargetId())
                            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "공연이 존재하지 않습니다."));
                        yield ProfileInfoDto.builder()
                            .profileId(performance.getPerformanceId())
                            .profileName(performance.getTitle())
                            .build();
                    }
                    case VENUE -> {
                        Venue venue = venueRepository.findById(relation.getTargetId())
                            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "공연장이 존재하지 않습니다."));
                        yield ProfileInfoDto.builder()
                            .profileId(venue.getVenueId())
                            .profileName(venue.getVenueName())
                            .build();
                    }
                    default ->
                        throw new ApiException(ErrorCode.NOT_FOUND, "알 수 없는 targetType: " + relation.getTargetType());
                };

                User loginUser = findProfileById(loginProfileId, loginProfileMode);

                // 로그인 사용자가 이 유저를 팔로우 중인지 확인
                boolean isFollowing =
                    userRelationRepository
                        .findByActor_UserIdAndActorProfileModeAndTargetIdAndTargetProfileModeAndRelationType(
                            loginUser.getUserId(),
                            loginProfileMode,
                            relation.getTargetId(),
                            relation.getTargetProfileMode(),
                            RelationType.FOLLOW
                        )
                        .filter(r -> !r.isDeleted())
                        .isPresent();

                return ResponseFollowListDto.builder()
                    .profileId(targetUser.getProfileId())
                    .userName(targetUser.getProfileName())
                    .profileMode(relation.getTargetProfileMode().name())
                    .isFollowing(isFollowing)
                    .build();
            })
            .toList();
        return result;
    }


    /**
     * 특정 프로필을 팔로우하고 있는 사용자(팔로워) 리스트 조회
     *
     * <p>동작 방식:
     * <ol>
     *   <li>targetId/targetProfileMode에 해당하는 팔로워 관계(UserRelation)를 조회합니다.</li>
     *   <li>각 관계의 actor(User)를 찾아 {@link ResponseFollowListDto}로 변환합니다.</li>
     *   <li>로그인 사용자가 각 팔로워를 팔로우 중인지 여부를 DTO에 반영합니다.</li>
     * </ol>
     * </p>
     *
     * @param targetId         팔로워 리스트를 조회할 대상 프로필 ID
     * @param targetMode       대상 프로필 모드
     * @param loginProfileId   로그인 사용자 프로필 ID
     * @param loginProfileMode 로그인 사용자 프로필 모드
     * @return 로그인 사용자 기준 팔로우 여부를 포함한 {@link ResponseFollowListDto} 리스트
     */
    public List<ResponseFollowListDto> getFollowerList(
        Long targetId, ActiveMode targetMode, Long loginProfileId, ActiveMode loginProfileMode) {

        Long targetUserId = getUserIdByProfile(targetId, targetMode);

        // 1️⃣ target 프로필을 팔로우하고 있는 관계 조회
        List<UserRelation> relations =
            userRelationRepository.findByTargetIdAndTargetProfileModeAndRelationTypeAndIsDeletedFalse(
                targetId,
                targetMode,
                RelationType.FOLLOW
            );

        User loginUser = findProfileById(loginProfileId, loginProfileMode);
        // 2️⃣ DTO 변환
        return relations.stream()
            .map(relation -> {
                // 팔로워(actor) 프로필 찾기
                ProfileInfoDto follower = getProfileInfoUserId(
                    relation.getActor().getUserId(),
                    relation.getActorProfileMode()
                );


                // 로그인 사용자가 팔로워를 팔로우 중인지 확인
                boolean isFollowing =
                    userRelationRepository.findByActor_UserIdAndActorProfileModeAndTargetIdAndTargetProfileModeAndRelationType(
                            loginUser.getUserId(),
                            loginProfileMode,
                            follower.getProfileId(),
                            relation.getActorProfileMode(),
                            RelationType.FOLLOW
                        )
                        .filter(r -> !r.isDeleted())
                        .isPresent();

                return ResponseFollowListDto.builder()
                    .profileId(follower.getProfileId())
                    .userName(follower.getProfileName())
                    .profileMode(relation.getActorProfileMode().name())
                    .isFollowing(isFollowing)
                    .build();
            })
            .toList();
    }

    public boolean isFollowing(Long loginUserId, ActiveMode loginProfileMode, Long profileId, ActiveMode activeMode) {
        boolean b = userRelationRepository.existsByActor_UserIdAndActorProfileModeAndTargetIdAndTargetProfileModeAndIsDeletedFalse(
            loginUserId, loginProfileMode, profileId, activeMode);

        return b;
    }

    /**
     * 타겟을 차단한다.
     *
     * @param profileId         차단하는 사람의 프로필 ID
     * @param profileMode       차단하는 사람의 프로필 모드
     * @param targetProfileId   차단 당할 사람의 프로필 아이디
     * @param targetProfileMode 차단 당할 사람의 프로필 모드
     * @return
     */
    public ResponseBlockDto block(Long profileId, ActiveMode profileMode, RequestBlockDto requestBlockDto) {
        validateBlockRequest(requestBlockDto);

        User actor = findProfileById(profileId, profileMode);

        // 1. 유저 타입일 때만 프로필 모드 파싱 (그 외에는 null)
        ActiveMode targetMode = (requestBlockDto.getTargetType() == TargetType.USER)
            ? ActiveMode.valueOf(requestBlockDto.getTargetProfileMode())
            : null;

        // 2. 기존 관계 조회 (targetType 조건이 들어간 새로운 쿼리 메서드 사용 권장)
        Optional<UserRelation> optionalRelation = userRelationRepository.findExistingRelationGeneral(
            actor.getUserId(),
            profileMode,
            requestBlockDto.getTargetId(),
            targetMode,
            requestBlockDto.getTargetType(),
            RelationType.BLOCK
        );
        UserRelation relation;

        if (optionalRelation.isPresent()) {
            relation = optionalRelation.get();
            if (relation.isDeleted()) {
                relation.restore();
                // 3. 유저 타입일 때만 팔로우 관계 청소
                if (requestBlockDto.getTargetType() == TargetType.USER) {
                    cleanUpFollows(actor.getUserId(), profileId, profileMode, requestBlockDto.getTargetId(), targetMode);
                }
            }
        } else {
            // 4. 새 관계 생성 (TargetType 반영)
            relation = UserRelation.builder()
                .actor(actor)
                .actorProfileMode(profileMode)
                .targetId(requestBlockDto.getTargetId())
                .targetProfileMode(targetMode)
                .targetType(requestBlockDto.getTargetType())
                .relationType(RelationType.BLOCK)
                .build();
            userRelationRepository.save(relation);

            if (requestBlockDto.getTargetType() == TargetType.USER) {
                cleanUpFollows(actor.getUserId(), profileId, profileMode, requestBlockDto.getTargetId(), targetMode);
            }
        }

        return ResponseBlockDto.builder()
            .targetId(requestBlockDto.getTargetId())
            .targetProfileMode(requestBlockDto.getTargetProfileMode())
            .isBlocked(true)
            .build();
    }

    /**
     * 차단 시 팔로우 해제 메소드
     *
     * @param actorUserId
     * @param actorProfileId
     * @param actorMode
     * @param targetProfileId
     * @param targetMode
     */
    private void cleanUpFollows(Long actorUserId, Long actorProfileId, ActiveMode actorMode,
                                Long targetProfileId, ActiveMode targetMode) {

        // 1. 내가(계정) 상대(프로필)를 팔로우한 것 삭제
        // 조건: actor_id = actorUserId AND target_id = targetProfileId
        userRelationRepository.deleteMyFollow(actorUserId, actorMode, targetProfileId, targetMode);

        User targetUser = findProfileById(targetProfileId, targetMode);

        // 2. 상대(프로필)가 나(프로필)를 팔로우한 것 삭제
        // 조건: actor_id = (상대의 계정ID) AND target_id = actorProfileId
        userRelationRepository.deleteTheirFollowToMe(targetUser.getUserId(), targetMode, actorProfileId, actorMode);
    }

    /**
     * 차단 해제 메소드
     *
     * @param profileId       차단 해제를 할 유저의 아이디
     * @param profileMode     차단 해제를 할 유저의 모드
     * @param requestBlockDto 차단 해제를 당할 target의 정보
     */
    public ResponseBlockDto unblockUser(Long profileId, ActiveMode profileMode, RequestBlockDto requestBlockDto) {

        validateBlockRequest(requestBlockDto);
        User actor = findProfileById(profileId, profileMode);

        ActiveMode targetMode = null;
        if (requestBlockDto.getTargetType() == TargetType.USER) {
            targetMode = ActiveMode.valueOf(requestBlockDto.getTargetProfileMode());
        }

        // TargetType을 포함하여 정확한 관계를 찾음
        UserRelation relation = userRelationRepository.findExistingRelationGeneral(
            actor.getUserId(),
            profileMode,
            requestBlockDto.getTargetId(),
            targetMode,
            requestBlockDto.getTargetType(),
            RelationType.BLOCK
        ).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "차단 내역이 존재하지 않습니다."));

        relation.delete(); // isDeleted = true

        return ResponseBlockDto.builder()
            .targetId(requestBlockDto.getTargetId())
            .targetProfileMode(requestBlockDto.getTargetProfileMode())
            .isBlocked(false)
            .build();
    }

    /**
     * 1. targetType이 유저인데 targetProfileMode가 들어있지 않을 경우 오류 발생
     * 2. targetType이 유저가 아닌데 targetProfileMode가 들어있을 경우 null값을 넣어 DB 정합성을 맞춤
     *
     * @param dto
     */
    private void validateBlockRequest(RequestBlockDto dto) {
        if (dto.getTargetType() == TargetType.USER) {
            if (dto.getTargetProfileMode() == null || dto.getTargetProfileMode().isBlank()) {
                throw new ApiException(ErrorCode.INVALID_REQUEST, "유저 차단 시 프로필 모드는 필수입니다.");
            }
        } else {
            // 유저가 아닌데 모드가 들어온 경우, 에러를 던지거나 null로 강제 초기화
            if (dto.getTargetProfileMode() != null) {
                dto.setTargetProfileMode(null);
            }
        }
    }
}

