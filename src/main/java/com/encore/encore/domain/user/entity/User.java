package com.encore.encore.domain.user.entity;

import com.encore.encore.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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

    // 이메일은 보통 50자 내외지만 넉넉하게 100자 설정
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // BCrypt 암호화 값(60자)을 저장하기 위해 최소 60자 이상 필요 (100자 권장)
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    // 닉네임은 한글 기준 20자 내외로 제한하는 경우가 많음 (50자 설정)
    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20) // Enum 이름 길이 고려
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20) // Enum 이름 길이 고려
    private UserStatus status;
}
