package com.encore.encore.domain.community.repository;

import com.encore.encore.domain.community.entity.Post;
import com.encore.encore.domain.community.entity.PostInteraction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostInteractionRepository extends JpaRepository<PostInteraction, Long> {

    /**
     * [설명] 특정 게시글에 대해 해당 공연자가 이미 신청했는지 확인합니다.
     * 
     * @param post                 게시글 엔티티
     * @param applicantPerformerId 신청한 공연자 ID
     * @param interactionType      상호작용 타입 (APPLY)
     * @return 존재 여부
     */
    boolean existsByPostAndApplicantPerformerIdAndInteractionTypeAndIsDeletedFalse(
            Post post,
            Long applicantPerformerId,
            String interactionType);
}
