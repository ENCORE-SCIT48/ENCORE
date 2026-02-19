package com.encore.encore.domain.chat.entity;

import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.global.common.BaseEntity;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "chat_post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "is_deleted = false")
public class ChatPost extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // String에서 Long으로 변경 권장 (IDENTITY 전략을 위해)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id")
    private Performance performance;
    @Column(nullable = false)
    private Long profileId; // 글쓴이 Id

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActiveMode profileMode;  // USER / PERFORMER / HOST
    private String title;
    private String content;
    private Integer maxMember;
    @Column(nullable = false)
    private Integer currentMember;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public enum Status {
        OPEN,
        CLOSED
    }

    public void setStatusFromString(String statusStr) {
        if (statusStr == null || statusStr.isBlank()) {
            // null 또는 빈 문자열일 때 INVALID_REQUEST 예외 던지기
            throw new ApiException(ErrorCode.INVALID_REQUEST);
        }
        try {
            this.status = Status.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Enum에 없는 값일 때도 INVALID_REQUEST 예외 던지기
            throw new ApiException(ErrorCode.INVALID_REQUEST);
        }
    }


    public void addParticipant() {
        if (this.currentMember >= this.maxMember) {
            throw new IllegalStateException("정원이 꽉 찼습니다.");
        }
        currentMember++;
    }

    public void removeParticipant() {
        if (currentMember > 0) currentMember--;
    }

}
