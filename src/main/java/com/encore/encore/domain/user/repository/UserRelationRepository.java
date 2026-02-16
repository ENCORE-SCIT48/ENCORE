package com.encore.encore.domain.user.repository;

import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.user.entity.RelationType;
import com.encore.encore.domain.user.entity.UserRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRelationRepository extends JpaRepository<UserRelation, Long> {
    Optional<UserRelation> findByActor_IdAndActorProfileModeAndTargetIdAndTargetProfileModeAndRelationType(Long profileId, ActiveMode profileMode, Long targetUserId, ActiveMode activeMode, RelationType relationType);
    
    List<UserRelation> findByActor_IdAndActorProfileModeAndRelationTypeAndIsDeletedFalse(Long targetId, ActiveMode targetMode, RelationType relationType);
    
    List<UserRelation> findByTargetIdAndTargetProfileModeAndRelationTypeAndIsDeletedFalse(Long targetId, ActiveMode targetMode, RelationType relationType);
}
