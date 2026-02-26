package com.encore.encore.domain.community.entity;

import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.user.entity.User;
import com.encore.encore.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActiveMode reporterProfileMode;
    @Column(nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportTargetType targetType;

    // "기타" 사유나 상세 설명을 저장할 필드
    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private String reasonDetail = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;


    // 신고 사유
    @Getter
    public enum ReportReason {
        SPAM("광고/도박/스팸"),
        ABUSE("욕설/비하발언"),
        INAPPROPRIATE_CONTENT("부적절한 콘텐츠"),
        SCAM("사기/허위정보"),
        PERSONAL_INFO("개인정보 노출"),
        ETC("기타(직접 입력)");

        private final String description;

        ReportReason(String description) {
            this.description = description;
        }
    }


    // 신고 처리 상태
    @Getter
    public enum ReportStatus {
        PENDING("대기"),
        UNDER_REVIEW("검토 중"),
        RESOLVED("조치 완료"),
        REJECTED("반려");

        private final String description;

        ReportStatus(String description) {
            this.description = description;
        }
    }
}
