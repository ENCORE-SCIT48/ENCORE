package com.encore.encore.domain.venue.repository;

import com.encore.encore.domain.venue.entity.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    Page<Venue> findByVenueNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
        String venueName,
        String address,
        Pageable pageable
    );
}
