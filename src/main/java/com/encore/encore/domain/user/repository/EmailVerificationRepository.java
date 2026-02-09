package com.encore.encore.domain.user.repository;

import com.encore.encore.domain.user.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    // 가장 최근의 삭제되지 않은 인증 정보 하나만 가져오기
    Optional<EmailVerification> findFirstByEmailAndCodeAndIsDeletedFalseOrderByCreatedAtDesc(String email, String code);
    // 이메일로 기존 인증 정보가 있는지 확인 (필요 시 기존 데이터 삭제 후 재발급용)
    void deleteByEmail(String email);

    @Query("SELECT ev FROM EmailVerification ev " +
        "WHERE ev.email = :email " +
        "AND ev.verified = true " +
        "AND ev.isDeleted = false " +
        "ORDER BY ev.createdAt DESC LIMIT 1")
    Optional<EmailVerification> findValidVerification(@Param("email") String email);
}
