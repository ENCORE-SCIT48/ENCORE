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

    /** 공연 대표 이미지(포스터) 경로 */
    @Column(name = "performance_image_url", length = 512)
    private String performanceImageUrl;

    /** 공연할 사람 모집 상태 (예: OPEN, CLOSED) */
    @Enumerated(EnumType.STRING)
    private PerformanceRecruitStatus recruitStatus;

    private Integer capacity;

    /** 공연 카테고리/장르 (MUSICAL / PLAY / BAND 등) */
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private PerformanceCategory category;

    /** 공연 진행 상태 (UPCOMING / ONGOING / ENDED / CANCELLED) */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PerformanceStatus status;
}
