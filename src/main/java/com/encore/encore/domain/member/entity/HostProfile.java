package com.encore.encore.domain.member.entity;

import com.encore.encore.domain.member.dto.HostProfileRequestDto;
import com.encore.encore.domain.user.entity.User;
import com.encore.encore.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "host_profiles")
public class HostProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "host_id")
    private Long hostId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_HOST_USER"))
    private User user;

    // [사업자 신원]
    @Column(name = "organization_name", nullable = false, length = 100)
    private String organizationName; // 법인/단체명

    @Column(name = "representative_name", length = 50)
    private String representativeName; // 대표자 성명 (국세청 진위확인용)

    @Column(name = "business_number", length = 20, unique = true)
    private String businessNumber; // 사업자 등록 번호

    @Column(name = "opening_date", length = 10)
    private String openingDate; // 개업일자 (YYYYMMDD, 국세청 진위확인용)

    // [비즈니스 연락처]
    @Column(name = "contact_number", length = 20)
    private String contactNumber; // 대표 연락처 (대관/비즈니스 문의용)

    @Column(name = "business_email", length = 100)
    private String businessEmail; // 공식 업무용 이메일

    // [상태 값]
    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false; // 국세청 API 인증 성공 여부

    @Builder.Default
    @Column(name = "is_initialized", nullable = false)
    private boolean isInitialized = false; // 필수 프로필 입력 완료 여부

    /**
     * [설명] 호스트 프로필 초기화 및 업데이트
     */
    public void initialize(HostProfileRequestDto dto) {
        if (dto.getOrganizationName() != null) this.organizationName = dto.getOrganizationName();
        if (dto.getRepresentativeName() != null) this.representativeName = dto.getRepresentativeName();
        if (dto.getBusinessNumber() != null) this.businessNumber = dto.getBusinessNumber().replaceAll("-", "");
        if (dto.getOpeningDate() != null) this.openingDate = dto.getOpeningDate().replaceAll("-", "");
        if (dto.getContactNumber() != null) this.contactNumber = dto.getContactNumber();
        if (dto.getBusinessEmail() != null) this.businessEmail = dto.getBusinessEmail();

        this.isInitialized = true;
    }

    /**
     * [설명] 국세청 API 검증 성공 시 호출되는 메서드
     */
    public void markAsVerified() {
        this.isVerified = true;
    }
}
