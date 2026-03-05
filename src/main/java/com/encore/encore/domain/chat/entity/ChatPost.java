package com.encore.encore.domain.chat.entity;

import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.global.common.BaseEntity;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

/**
 * 공연 소속 채팅 게시글(모집글) 엔티티.
 * <p>
 * 기획: 공연 하나당 여러 채팅방이 붙으며, "공연을 본 뒤 이어지는 공간"으로
 * 후기·감상, 택시 동승, 뒤풀이 등 목적별로 구분한다. {@link #postType}으로 구분.
 * </p>
 *
 * @see ChatRoom#getRoomType() PERFORMANCE_ALL(공연 전체 톡) vs CHAT(개별 모집방)
 * @see ChatPostType
 */
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
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id")
    private Performance performance;
    @Column(nullable = false)
    private Long profileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActiveMode profileMode;
    /** 공연 채팅 목적: 후기/택시/뒤풀이/일반. null이면 GENERAL로 간주. */
    @Enumerated(EnumType.STRING)
    @Column(name = "post_type")
    private ChatPostType postType;
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
