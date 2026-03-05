package com.encore.encore.domain.community.entity;

import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.domain.user.entity.User;
import com.encore.encore.domain.venue.entity.Seat;
import com.encore.encore.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id", nullable = false)
    private Performance performance;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    private Seat seat;
    private Integer rating;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;
    /** Encore pick: 이 공연에서 가장 기억에 남는 곡/장면 한 줄 (공연 리뷰만 사용, 좌석 리뷰는 null) */
    @Column(length = 200)
    private String encorePick;
}
