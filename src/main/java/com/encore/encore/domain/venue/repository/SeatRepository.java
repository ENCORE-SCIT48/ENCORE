package com.encore.encore.domain.venue.repository;

import com.encore.encore.domain.venue.entity.Seat;
import com.encore.encore.domain.venue.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    /**
     * [설명] 특정 공연장의 모든 좌석을 조회 (논리 삭제 처리용)
     */
    List<Seat> findByVenue(Venue venue);

    /**
     * [설명] 특정 공연장의 삭제되지 않은 좌석만 조회 (화면 표시용)
     */
    List<Seat> findAllByVenueAndIsDeletedFalse(Venue venue);

    /**
     * [설명] 특정 공연장의 모든 좌석 정보를 물리 삭제합니다.
     * [주의] 예약 데이터(Reservation)가 존재할 경우 외래키 제약조건 위반이 발생할 수 있습니다.
     */
    @Modifying
    @Query("DELETE FROM Seat s WHERE s.venue = :venue")
    void deleteByVenue(@Param("venue") Venue venue);
}
