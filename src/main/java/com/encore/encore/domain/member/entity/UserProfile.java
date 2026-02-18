package com.encore.encore.domain.member.entity;

import com.encore.encore.domain.member.dto.UserProfileRequestDto;
import com.encore.encore.domain.user.entity.User;
import com.encore.encore.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "user_profile")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileId;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 초기 설정 완료 여부
    @Column(nullable = false)
    @Builder.Default
    private boolean isInitialized = false;

    @Column(name = "profile_image_url", length = 512)
    private String profileImageUrl;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "location", length = 100)
    private String location;

    // 복수 선택 필드: "ROCK,POP" 형태의 문자열로 저장
    @Column(name = "preferred_genres", nullable = false)
    private String preferredGenres;

    // 복수 선택 필드: "STANDING,SEATING" 형태의 문자열로 저장
    @Column(name = "preferred_performance_types", nullable = false)
    private String preferredPerformanceTypes;

    @Column(name = "introduction", columnDefinition = "TEXT")
    private String introduction;

    /**
     * 프로필 정보를 초기화하거나 업데이트합니다.
     * null 값 방지.
     */
    public void initialize(UserProfileRequestDto dto, String imageUrl) {
        // 1. 일반 필드 업데이트 (null이 아닐 때만 유지하거나 업데이트)
        if (dto.getPhoneNumber() != null) this.phoneNumber = dto.getPhoneNumber();
        if (dto.getLocation() != null) this.location = dto.getLocation();
        if (dto.getIntroduction() != null) this.introduction = dto.getIntroduction();
        if (dto.getBirthDate() != null) this.birthDate = dto.getBirthDate();

        // 2. 이미지 URL 처리 (새로운 이미지가 업로드된 경우에만 교체)
        if (imageUrl != null && !imageUrl.isBlank()) {
            this.profileImageUrl = imageUrl;
        }

        // 3. 리스트 필드 처리 (데이터가 넘어왔을 때만 join하여 저장)
        if (dto.getPreferredGenres() != null && !dto.getPreferredGenres().isEmpty()) {
            this.preferredGenres = String.join(",", dto.getPreferredGenres());
        }

        if (dto.getPreferredPerformanceTypes() != null && !dto.getPreferredPerformanceTypes().isEmpty()) {
            this.preferredPerformanceTypes = String.join(",", dto.getPreferredPerformanceTypes());
        }

        // 초기화 완료 플래그 설정
        this.isInitialized = true;
    }
}
