package com.encore.encore.domain.community.entity;

import com.encore.encore.domain.member.entity.PerformerProfile;
import com.encore.encore.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_interaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostInteraction extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_performer_id", nullable = false)
    private PerformerProfile applicantPerformer;
    
    private Long targetPerformerId;
    private Long senderPerformerId;
    private String interactionType;
    private String status;
    private String message;
}
