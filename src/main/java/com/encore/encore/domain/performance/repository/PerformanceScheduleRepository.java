package com.encore.encore.domain.performance.repository;

import com.encore.encore.domain.performance.entity.PerformanceSchedule;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PerformanceScheduleRepository extends JpaRepository<PerformanceSchedule, Long> {

    /**
     * [피드 1]
     * 내가 찜한 공연 중, 시작 시간이 now~to 사이인 스케줄 목록 조회
     * - 찜(status='WISHED') 기준
     * - 시작시간 오름차순
     */
    @Query("""
        select ps
        from PerformanceSchedule ps
            join fetch ps.performance p
            join com.encore.encore.domain.performance.entity.UserPerformanceRelation upr
                on upr.performance.performanceId = p.performanceId
        where upr.user.userId = :userId
          and upr.status = :status
          and upr.isDeleted = false
          and ps.isDeleted = false
          and ps.startTime >= :now
          and ps.startTime <= :to
        order by ps.startTime asc
    """)
    List<PerformanceSchedule> findUpcomingWishedSchedules(
        @Param("userId") Long userId,
        @Param("status") String status,
        @Param("now") LocalDateTime now,
        @Param("to") LocalDateTime to
    );

    /**
     * [피드 2]
     * 팔로우한 사람들이 찜한 공연 스케줄 조회(최신/가까운 시작 중심)
     * - 간단하게 "스케줄 1건=피드 1건"으로 만든 버전
     * - 같은 공연이 여러 회차면 여러 건 뜰 수 있음(초기 개발 단계에서는 OK)
     */
    @Query("""
        select
            ps.startTime as startTime,
            p.performanceId as performanceId,
            p.title as title,
            u.userId as actorUserId,
            u.nickname as actorNickname
        from PerformanceSchedule ps
            join ps.performance p
            join com.encore.encore.domain.performance.entity.UserPerformanceRelation upr
                on upr.performance.performanceId = p.performanceId
            join upr.user u
        where u.userId in :followedUserIds
          and upr.status = :status
          and upr.isDeleted = false
          and ps.isDeleted = false
          and ps.startTime >= :now
        order by ps.startTime asc
    """)
    List<FollowWishedRow> findFollowWishedSchedules(
        @Param("followedUserIds") List<Long> followedUserIds,
        @Param("status") String status,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );

    interface FollowWishedRow {
        LocalDateTime getStartTime();
        Long getPerformanceId();
        String getTitle();
        Long getActorUserId();
        String getActorNickname();
    }
}
