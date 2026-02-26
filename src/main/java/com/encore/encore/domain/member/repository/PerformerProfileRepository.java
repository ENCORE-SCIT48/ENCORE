package com.encore.encore.domain.member.repository;

import com.encore.encore.domain.member.entity.PerformerProfile;
import com.encore.encore.domain.user.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PerformerProfileRepository extends JpaRepository<PerformerProfile, Long> {
    // UserProfileRepository.java
    Optional<PerformerProfile> findByUser_UserId(Long userId);

    Optional<PerformerProfile> findByUser(User user);

    boolean existsByUser(User user); // 공연자 프로필 존재 여부 체크 (접근 검증용)

    /**
     * [설명] 로그인한 사용자를 제외한 공연자 프로필 목록을 조회합니다.
     *
     * - 논리 삭제(isDeleted = false)된 데이터는 제외합니다.
     * - 본인(userId)은 결과에서 제외됩니다.
     *
     * @param userId 로그인한 사용자 ID
     * @return 본인을 제외한 공연자 프로필 목록
     */
    List<PerformerProfile> findByUser_UserIdNotAndIsDeletedFalse(Long userId);

    /**
     * [설명] 로그인 사용자를 제외하고,
     * 무대명, 활동 지역, 포지션 조건으로 공연자 목록을 조회합니다.
     *
     * - 논리 삭제(isDeleted = false)된 데이터는 제외합니다.
     * - 로그인 사용자(userId)는 결과에서 제외됩니다.
     * - keyword가 존재하면 무대명 또는 포지션 기준 검색합니다.
     * - activityArea가 존재하면 활동 지역 LIKE 검색합니다.
     * - part가 존재하면 포지션 LIKE 검색합니다.
     * - Pageable을 이용하여 페이징 처리합니다.
     *
     * @param userId       로그인 사용자 ID
     * @param keyword      무대명 검색 키워드 (nullable)
     * @param activityArea 활동 지역 필터 (nullable)
     * @param part         포지션 필터 (nullable)
     * @param pageable     페이징 정보
     * @return 조건에 맞는 공연자 페이징 결과
     */
    @Query("""
                SELECT p FROM PerformerProfile p
                WHERE p.user.userId <> :userId
                AND p.isDeleted = false
                AND (
                    :keyword IS NULL OR :keyword = ''
                    OR LOWER(p.stageName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(p.part) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
                AND (:activityArea IS NULL OR :activityArea = ''
                    OR LOWER(p.activityArea) LIKE LOWER(CONCAT('%', :activityArea, '%')))
                AND (:part IS NULL OR :part = ''
                    OR LOWER(p.part) LIKE LOWER(CONCAT('%', :part, '%')))
            """)
    Page<PerformerProfile> searchWithFilter(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("activityArea") String activityArea,
            @Param("part") String part,
            Pageable pageable);
}
