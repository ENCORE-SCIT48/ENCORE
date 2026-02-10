package com.encore.encore.domain.member.entity;

import com.encore.encore.domain.user.entity.User;
import com.encore.encore.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "host_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostProfile extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hostId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    // 초기 설정 완료 여부
    @Column(nullable = false)
    @Builder.Default
    private boolean isInitialized = false;

    private String organizationName;
    private String businessNumber;

}
