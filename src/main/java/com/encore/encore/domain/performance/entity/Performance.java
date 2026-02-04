package com.encore.encore.domain.performance.entity;

import com.encore.encore.domain.member.entity.HostProfile;
import com.encore.encore.domain.member.entity.PerformerProfile;
import com.encore.encore.domain.venue.entity.Venue;
import com.encore.encore.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "performance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Performance extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long performanceId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    private Venue venue;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_creator_id")
    private HostProfile hostCreator;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performer_creator_id")
    private PerformerProfile performerCreator;
    private String title;
    private String description;
    private String recruitStatus;
    private Integer capacity;
    private String status;
}
