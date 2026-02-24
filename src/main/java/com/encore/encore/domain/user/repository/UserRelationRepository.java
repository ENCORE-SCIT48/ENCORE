package com.encore.encore.domain.user.repository;

import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.user.entity.RelationType;
import com.encore.encore.domain.user.entity.TargetType;
import com.encore.encore.domain.user.entity.UserRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRelationRepository extends JpaRepository<UserRelation, Long> {

    /**
     * 로그인 유저가 이전의 유저와 관계를 맺은 데이터가 존재하는지 조회
     *
     * @param actorId    로그인 유저의 유저id
     * @param actorMode  로그인 유저의 프로필 모드
     * @param targetId   타겟 유저의 프로필id
     * @param targetMode 타겟 유저의 프로필 모드
     * @param type       프로필 타입
     * @return
     */
    @Query("SELECT r FROM UserRelation r " +
        "WHERE r.actor.userId = :actorId " +
        "AND r.actorProfileMode = :actorMode " +
        "AND r.targetId = :targetId " +
        "AND r.targetProfileMode = :targetMode " +
        "AND r.relationType = :type")
    Optional<UserRelation> findExistingRelation(
        @Param("actorId") Long actorId,
        @Param("actorMode") ActiveMode actorMode,
        @Param("targetId") Long targetId,
        @Param("targetMode") ActiveMode targetMode,
        @Param("type") RelationType type
    );

    List<UserRelation> findByActor_UserIdAndActorProfileModeAndRelationTypeAndIsDeletedFalse(Long targetId, ActiveMode targetMode, RelationType relationType);

    List<UserRelation> findByTargetIdAndTargetProfileModeAndRelationTypeAndIsDeletedFalse(Long targetId, ActiveMode targetMode, RelationType relationType);

    Optional<UserRelation> findByActor_UserIdAndActorProfileModeAndTargetIdAndTargetProfileModeAndRelationType(Long actorUserId, ActiveMode profileMode, Long targetProfileId, ActiveMode targetMode, RelationType relationType);

    @Query("""
            select r.targetId
            from UserRelation r
            where r.actor.userId = :userId
              and r.relationType = :relationType
              and r.isDeleted = false
        """)
    List<Long> findTargetIdsByActorAndRelationType(
        @Param("userId") Long userId,
        @Param("relationType") RelationType relationType
    );

    /**
     * 팔로잉 수 조회
     *
     * @param actorId
     * @param actorMode
     * @return
     */
    @Query("SELECT COUNT(r) FROM UserRelation r " +
        "WHERE r.actor.userId = :actorId " +
        "AND r.actorProfileMode = :actorMode " +
        "AND r.relationType = 'FOLLOW' " +
        "AND r.isDeleted = false")
    int countFollowing(
        @Param("actorId") Long actorId,
        @Param("actorMode") ActiveMode actorMode
    );

    /**
     * 팔로워 수 조회
     *
     * @param targetId
     * @param targetMode
     * @return
     */
    @Query("SELECT COUNT(r) FROM UserRelation r " +
        "WHERE r.targetId = :targetId " +
        "AND r.targetProfileMode = :targetMode " +
        "AND r.relationType = 'FOLLOW' " +
        "AND r.isDeleted = false")
    int countFollower(
        @Param("targetId") Long targetId,
        @Param("targetMode") ActiveMode targetMode
    );

    // 로그인 유저가 해당 유저를 팔로우 했는가 판단
    boolean existsByActor_UserIdAndActorProfileModeAndTargetIdAndTargetProfileModeAndRelationTypeAndIsDeletedFalse(
        Long loginUserId,
        ActiveMode loginProfileMode,
        Long profileId,
        ActiveMode activeMode,
        RelationType type  // 여기에 RelationType.FOLLOW 를 넘겨줌
    );

    // 1. 내가(계정) 상대(프로필)를 팔로우한 관계 삭제
    @Modifying
    @Query("UPDATE UserRelation ur SET ur.isDeleted = true " +
        "WHERE ur.relationType = 'FOLLOW' AND ur.isDeleted = false " +
        "AND ur.actor.userId = :myUserId AND ur.actorProfileMode = :myMode " +
        "AND ur.targetId = :targetProfileId AND ur.targetProfileMode = :targetMode")
    void deleteMyFollow(Long myUserId, ActiveMode myMode, Long targetProfileId, ActiveMode targetMode);

    // 2. 상대(계정)가 나(프로필)를 팔로우한 관계 삭제
    @Modifying
    @Query("UPDATE UserRelation ur SET ur.isDeleted = true " +
        "WHERE ur.relationType = 'FOLLOW' AND ur.isDeleted = false " +
        "AND ur.actor.userId = :theirUserId AND ur.actorProfileMode = :theirMode " +
        "AND ur.targetId = :myProfileId AND ur.targetProfileMode = :myMode")
    void deleteTheirFollowToMe(Long theirUserId, ActiveMode theirMode, Long myProfileId, ActiveMode myMode);

    @Query("SELECT r FROM UserRelation r " +
        "WHERE r.actor.userId = :actorId " +
        "AND r.actorProfileMode = :actorMode " +
        "AND r.targetId = :targetId " +
        "AND r.targetType = :targetType " + // 타겟 타입 추가
        "AND (:targetMode IS NULL OR r.targetProfileMode = :targetMode) " + // NULL 대응
        "AND r.relationType = :type")
    Optional<UserRelation> findExistingRelationGeneral(
        @Param("actorId") Long actorId,
        @Param("actorMode") ActiveMode actorMode,
        @Param("targetId") Long targetId,
        @Param("targetMode") ActiveMode targetMode,
        @Param("targetType") TargetType targetType, // 파라미터 추가
        @Param("type") RelationType type
    );

    // 차단 관계 조회
    List<UserRelation> findAllByActor_UserIdAndActorProfileModeAndRelationTypeAndIsDeletedFalse(Long userId, ActiveMode profileMode, RelationType relationType);

}
