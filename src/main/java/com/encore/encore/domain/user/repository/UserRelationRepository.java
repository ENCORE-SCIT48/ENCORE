package com.encore.encore.domain.user.repository;

import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.user.entity.RelationType;
import com.encore.encore.domain.user.entity.UserRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRelationRepository extends JpaRepository<UserRelation, Long> {
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
            @Param("type") RelationType type);

    List<UserRelation> findByActor_UserIdAndActorProfileModeAndRelationTypeAndIsDeletedFalse(Long targetId,
            ActiveMode targetMode, RelationType relationType);

    List<UserRelation> findByTargetIdAndTargetProfileModeAndRelationTypeAndIsDeletedFalse(Long targetId,
            ActiveMode targetMode, RelationType relationType);

    Optional<UserRelation> findByActor_UserIdAndActorProfileModeAndTargetIdAndTargetProfileModeAndRelationType(
            Long actorUserId, ActiveMode profileMode, Long targetProfileId, ActiveMode targetMode,
            RelationType relationType);

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
            @Param("actorMode") ActiveMode actorMode);

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
            @Param("targetMode") ActiveMode targetMode);

    boolean existsByActor_UserIdAndActorProfileModeAndTargetIdAndTargetProfileModeAndIsDeletedFalse(Long loginUserId,
            ActiveMode loginProfileMode, Long profileId, ActiveMode activeMode);

    List<UserRelation> findByActor_UserIdAndActorProfileModeAndRelationTypeAndIsDeletedFalse(Long targetId,
            ActiveMode targetMode, RelationType relationType);

    List<UserRelation> findByTargetIdAndTargetProfileModeAndRelationTypeAndIsDeletedFalse(Long targetId,
            ActiveMode targetMode, RelationType relationType);

    Optional<UserRelation> findByActor_UserIdAndActorProfileModeAndTargetIdAndTargetProfileModeAndRelationType(
            Long actorUserId, ActiveMode profileMode, Long targetProfileId, ActiveMode targetMode,
            RelationType relationType);

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
            @Param("actorMode") ActiveMode actorMode);

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
            @Param("targetMode") ActiveMode targetMode);

    boolean existsByActor_UserIdAndActorProfileModeAndTargetIdAndTargetProfileModeAndIsDeletedFalse(Long loginUserId,
            ActiveMode loginProfileMode, Long profileId, ActiveMode activeMode);

    List<UserRelation> findByActor_UserIdAndActorProfileModeAndRelationTypeAndIsDeletedFalse(Long targetId,
            ActiveMode targetMode, RelationType relationType);

    List<UserRelation> findByTargetIdAndTargetProfileModeAndRelationTypeAndIsDeletedFalse(Long targetId,
            ActiveMode targetMode, RelationType relationType);

    Optional<UserRelation> findByActor_UserIdAndActorProfileModeAndTargetIdAndTargetProfileModeAndRelationType(
            Long actorUserId, ActiveMode profileMode, Long targetProfileId, ActiveMode targetMode,
            RelationType relationType);

}
