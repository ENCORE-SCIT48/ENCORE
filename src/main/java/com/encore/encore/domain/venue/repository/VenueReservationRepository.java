package com.encore.encore.domain.venue.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.encore.encore.domain.venue.entity.VenueReservation;
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


}
