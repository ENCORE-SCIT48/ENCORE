package com.encore.encore.domain.venue.repository;

import com.encore.encore.domain.venue.entity.ReservationStatus;
import com.encore.encore.domain.venue.entity.Venue;
import com.encore.encore.domain.venue.entity.VenueReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * VenueReservation JPA 리포지토리.
 * N+1 방지를 위해 연관 엔티티를 fetch join 으로 함께 조회한다.
 */
public interface VenueReservationRepository extends JpaRepository<VenueReservation, Long> {

    /**
     * 특정 공연자의 대관 요청 목록을 최신순으로 조회한다.
     *
     * @param performerId 공연자 프로필 ID
     * @return 해당 공연자의 전체 예약 목록
     */
    @Query("""
        SELECT r FROM VenueReservation r
        JOIN FETCH r.venue v
        JOIN FETCH r.host h
        JOIN FETCH r.performer p
        WHERE p.performerId = :performerId
        ORDER BY r.createdAt DESC
        """)
    List<VenueReservation> findAllByPerformerIdWithDetails(@Param("performerId") Long performerId);

    /**
     * 특정 공연장에 접수된 대관 요청 목록을 최신순으로 조회한다.
     *
     * @param venueId 공연장 ID
     * @return 해당 공연장의 전체 예약 목록
     */
    @Query("""
        SELECT r FROM VenueReservation r
        JOIN FETCH r.venue v
        JOIN FETCH r.host h
        JOIN FETCH r.performer p
        WHERE v.venueId = :venueId
        ORDER BY r.createdAt DESC
        """)
    List<VenueReservation> findAllByVenueIdWithDetails(@Param("venueId") Long venueId);

    /**
     * 동일 공연장에 겹치는(PENDING/APPROVED) 예약이 존재하는지 여부를 확인한다.
     *
     * @param venue   공연장
     * @param status  포함할 예약 상태 목록
     * @param startAt 시작 일시
     * @param endAt   종료 일시
     * @return 겹치는 예약 존재 여부
     */
    boolean existsByVenueAndStatusInAndEndAtGreaterThanAndStartAtLessThan(
        Venue venue,
        Collection<ReservationStatus> status,
        LocalDateTime startAt,
        LocalDateTime endAt
    );
}
