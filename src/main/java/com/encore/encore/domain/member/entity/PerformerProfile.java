    package com.encore.encore.domain.member.entity;

    import com.encore.encore.domain.member.dto.PerformerProfileRequestDto;
    import com.encore.encore.domain.user.entity.User;
    import com.encore.encore.global.common.BaseEntity;
    import jakarta.persistence.*;
    import lombok.*;

    @Entity
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Table(name = "performer_profiles")
    public class PerformerProfile extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long performerId;

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @Column(nullable = false, length = 100)
        private String stageName;

        /**
         * [설명] 선택한 카테고리(장르) 리스트를 콤마(,)로 구분된 문자열로 저장합니다.
         * [이유] DB 설계를 단순화하면서 다중 선택 기능을 구현하기 위함.
         */
        @Column(length = 255)
        private String category;

        @Column(columnDefinition = "TEXT")
        private String description;
        private String activityArea;

        private String part;

        @Enumerated(EnumType.STRING)
        private SkillLevel skillLevel;

        @Builder.Default
        private boolean isInitialized = false;


        /**
         * [설명] 프로필 초기화 및 업데이트
         * [변경사항] List<String> 형태의 카테고리를 String.join을 통해 저장합니다.
         */
        public void initialize(PerformerProfileRequestDto dto) {
            if (dto.getStageName() != null) this.stageName = dto.getStageName();
            if (dto.getDescription() != null) this.description = dto.getDescription();
            if (dto.getActivityArea() != null) this.activityArea = dto.getActivityArea();
            if (dto.getPart() != null) this.part = dto.getPart();

            // 2. 실력 등급 업데이트
            if (dto.getSkillLevel() != null) {
                this.skillLevel = SkillLevel.from(dto.getSkillLevel());
            }

            // 카테고리 다중 선택 처리 (List -> String)
            if (dto.getCategories() != null && !dto.getCategories().isEmpty()) {
                this.category = String.join(",", dto.getCategories());
            }

            this.isInitialized = true;

        }
    }
