package com.encore.encore.domain.performance.entity;

import com.encore.encore.domain.member.entity.PerformerProfile;
import com.encore.encore.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "performance_performer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformancePerformer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long performancePerformerId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id", nullable = false)
    private Performance performance;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performer_id")
    private PerformerProfile performer;
    private Long artistProfileId;
    private String role;
    private LocalDateTime confirmedAt;
    private Long applicantPerformerId;
    private String status;
    private Integer rankNo;
}
