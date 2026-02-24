package com.encore.encore.domain.venue.repository;

import com.encore.encore.domain.venue.entity.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    Page<Venue> findByVenueNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
        String venueName,
        String address,
        Pageable pageable
    );

    @Query("SELECT v.venueName FROM Venue v WHERE v.venueId = :venueId")
    String findVenueNameByVenueId(@Param("venueId") Long venueId);
}
