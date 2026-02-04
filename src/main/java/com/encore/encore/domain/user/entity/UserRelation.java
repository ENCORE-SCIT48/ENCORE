package com.encore.encore.domain.user.entity;

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
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long relationId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "actor_id", nullable = false)
    private User actor;
    private String relationType;
    private String targetType;
    private Long targetId;
}
