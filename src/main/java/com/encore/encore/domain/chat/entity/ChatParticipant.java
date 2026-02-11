package com.encore.encore.domain.chat.entity;

import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_participant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatParticipant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long participantId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    @Column(nullable = false)
    private Long profileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActiveMode profileMode;  // USER / PERFORMER / HOST

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantStatus participantStatus;

    public enum ParticipantStatus {
        PENDING,
        ACCEPTED,
        REJECTED;

    }

}
