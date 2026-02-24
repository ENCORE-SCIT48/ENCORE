package com.encore.encore.domain.performance.repository;

import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.domain.performance.entity.UserPerformanceRelation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserPerformanceRelationRepository extends JpaRepository<UserPerformanceRelation, Long> {

    /**
     * 상태 존재 여부 확인
     * - 예) 본 공연 여부(WATCHED), 찜 여부(WISHED)
     * - is_deleted=false인 현재 유효한 관계만 대상으로 함
     */
    boolean existsByUser_UserIdAndPerformance_PerformanceIdAndStatusAndIsDeletedFalse(
        Long userId,
        Long performanceId,
        String status
    );

    /**
     * 토글(찜 on/off) 시 사용
     * - 현재 활성 상태(is_deleted=false)인 row 중 가장 최신 1건을 가져옴
     * - off 처리할 때 delete()로 논리삭제(is_deleted=true) 처리
     *
     * ※ order 기준은 relationId(자동증가)로 최신 판단
     *   (createdAt을 쓰는 방식도 가능하지만, 지금 구조에서는 relationId가 가장 단순/확실)
     */
    Optional<UserPerformanceRelation> findTopByUser_UserIdAndPerformance_PerformanceIdAndStatusAndIsDeletedFalseOrderByRelationIdDesc(
        Long userId,
        Long performanceId,
        String status
    );

    /**
     * 본 공연 리스트(WATCHED) 조회 + 검색 지원
     * - UserPerformanceRelation을 그대로 내려보내면 LAZY 직렬화 이슈가 생길 수 있어
     *   Performance만 바로 뽑아서 Service에서 DTO로 변환하는 방식
     * - watchedAt DESC: 본 날짜 기준 최신순
     * - keyword가 null/빈문자면 전체 조회(검색 조건 무시)
     */
    @Query("""
        select upr.performance
        from UserPerformanceRelation upr
        where upr.user.userId = :userId
          and upr.status = :status
          and upr.isDeleted = false
          and (
                :keyword is null
                or :keyword = ''
                or lower(upr.performance.title) like lower(concat('%', :keyword, '%'))
          )
        order by upr.watchedAt desc
    """)
    Page<Performance> findPerformancesByUserIdAndStatusAndKeywordOrderByWatchedAtDesc(
        @Param("userId") Long userId,
        @Param("status") String status,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    /**
     * "북마크/찜 리스트(WISHED)" 조회 + 검색 지원
     * - createdAt DESC: 찜한(관계 생성) 최신순
     * - keyword가 null/빈문자면 전체 조회(검색 조건 무시)
     */
    @Query("""
        select upr.performance
        from UserPerformanceRelation upr
        where upr.user.userId = :userId
          and upr.status = :status
          and upr.isDeleted = false
          and (
                :keyword is null
                or :keyword = ''
                or lower(upr.performance.title) like lower(concat('%', :keyword, '%'))
          )
        order by upr.createdAt desc
    """)
    Page<Performance> findPerformancesByUserIdAndStatusAndKeywordOrderByCreatedAtDesc(
        @Param("userId") Long userId,
        @Param("status") String status,
        @Param("keyword") String keyword,
        Pageable pageable
    );
}
