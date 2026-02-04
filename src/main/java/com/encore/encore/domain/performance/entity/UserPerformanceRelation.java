package com.encore.encore.domain.performance.entity;

import com.encore.encore.domain.user.entity.User;
import com.encore.encore.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_performance_relation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPerformanceRelation extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long relationId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "performance_id")
    private Performance performance;
    private String status;
    @Column(nullable = false) private LocalDateTime watchedAt;
}
