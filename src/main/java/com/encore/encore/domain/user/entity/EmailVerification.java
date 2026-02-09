package com.encore.encore.domain.user.entity;

import com.encore.encore.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long verificationId;

    @Column(nullable = false)
    private String email; // User 참조 대신 이메일 직접 저장

    @Column(nullable = false, length = 6)
    private String code; // 6자리 인증번호

    @Column(nullable = false)
    private LocalDateTime expiredAt; // 만료 시간

    @Builder.Default
    private Boolean verified = false; // 인증 성공 여부

    // 만료 여부 확인 로직
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiredAt);
    }
}
