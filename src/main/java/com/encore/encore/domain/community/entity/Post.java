package com.encore.encore.domain.community.entity;

import com.encore.encore.domain.member.entity.HostProfile;
import com.encore.encore.domain.member.entity.PerformerProfile;
import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.domain.venue.entity.Venue;
import com.encore.encore.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id")
    private Performance performance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_author_id")
    private HostProfile hostAuthor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performer_author_id")
    private PerformerProfile performerAuthor;

    @Column(nullable = false)
    private Integer capacity;

    private String postType;

    private String title;

    private String content;

    private Integer viewCount;
}
