package com.encore.encore.domain.user.entity;

import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_relation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRelation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long relationId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActiveMode actorProfileMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelationType relationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetType targetType;

    private Long targetId;

    @Enumerated(EnumType.STRING)
    private ActiveMode targetProfileMode;

}
