package com.encore.encore.domain.user.entity;

import com.encore.encore.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20) // Enum 이름 길이 고려
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20) // Enum 이름 길이 고려
    private UserStatus status;

    @Column(nullable = false)
    private Boolean agreeTerms;      // [필수] 서비스 이용약관 동의

    @Column(nullable = false)
    private Boolean agreePrivacy;    // [필수] 개인정보 수집 및 이용 동의

    @Column(nullable = false)
    private Boolean agreeMarketing;  // [선택] 마케팅 정보 수신 동의 (선택도 저장은 해야 함)

    @Column
    private LocalDateTime agreedAt;  // 동의한 시간 (가입 시간과 동일할 수도 있지만 명시적으로 관리)
}
