package com.encore.encore.domain.venue.entity;

import com.encore.encore.domain.member.entity.HostProfile;
import com.encore.encore.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "venue")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venue extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long venueId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "host_id", nullable = false)
    private HostProfile host;
    @Column(nullable = false) private String venueName;
    @Column(nullable = false) private String address;
    private String venueType;
    private Integer totalSeats;
}
