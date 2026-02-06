package com.encore.encore.domain.chat.entity;

import com.encore.encore.domain.member.entity.HostProfile;
import com.encore.encore.domain.member.entity.PerformerProfile;
import com.encore.encore.domain.member.entity.UserProfile;
import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.global.common.BaseEntity;
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id")
    private HostProfile host;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private UserProfile profile;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performer_id")
    private PerformerProfile performer;
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
