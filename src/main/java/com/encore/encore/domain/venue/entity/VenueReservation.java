package com.encore.encore.domain.venue.entity;

import com.encore.encore.domain.member.entity.HostProfile;
import com.encore.encore.domain.member.entity.PerformerProfile;
import com.encore.encore.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "venue_reservation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueReservation extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "host_id")
    private HostProfile host;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "performer_id")
    private PerformerProfile performer;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String status;
}
