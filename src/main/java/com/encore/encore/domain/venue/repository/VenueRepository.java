package com.encore.encore.domain.venue.repository;

import com.encore.encore.domain.venue.entity.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    Page<Venue> findByVenueNameContainingIgnoreCaseAndIsDeletedFalseOrAddressContainingIgnoreCaseAndIsDeletedFalse(
        String venueName,
        String address,
        Pageable pageable
    );

    Optional<Venue> findByVenueIdAndIsDeletedFalse(Long venueId);

    /**
     * 삭제되지 않은 공연장 목록 전체를 페이징 조회합니다.
     */
    Page<Venue> findByIsDeletedFalse(Pageable pageable);
    @Query("SELECT v.venueName FROM Venue v WHERE v.venueId = :venueId")
    String findVenueNameByVenueId(@Param("venueId") Long venueId);

    /**
     * 삭제되지 않은 공연장 목록 전체를 페이징 조회합니다.
     */


    // [추가] GET /api/venues/my 용 — 호스트 소유 공연장 목록 (삭제 제외)
    List<Venue> findByHost_HostIdAndIsDeletedFalse(Long hostId);
}
