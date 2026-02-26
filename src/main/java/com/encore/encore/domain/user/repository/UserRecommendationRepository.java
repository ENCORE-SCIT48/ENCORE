package com.encore.encore.domain.user.repository;

import com.encore.encore.domain.performance.entity.UserPerformanceRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRecommendationRepository extends JpaRepository<UserPerformanceRelation, Long> {

    /**
     * 로그인 유저와 같은 공연을 2개 이상 본 유저의 id를 조회
     *
     * @param userId
     * @param minCount
     * @return
     */
    @Query("""
            SELECT upr2.user.userId
            FROM UserPerformanceRelation upr1
            JOIN UserPerformanceRelation upr2
                ON upr1.performance.performanceId = upr2.performance.performanceId
            WHERE upr1.user.userId = :userId
              AND upr2.user.userId <> :userId
            GROUP BY upr2.user.userId
            HAVING COUNT(upr2.user.userId) >= :minCount
        """)
    List<Long> findUserIdsWithCommonPerformance(
        @Param("userId") Long userId,
        @Param("minCount") Long minCount
    );
}
