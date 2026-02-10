package com.encore.encore.domain.chat.entity;

import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;
    @Column(nullable = false)
    private Long profileId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActiveMode profileMode;
    @Column(nullable = false)
    private String content;
    private LocalDateTime sentAt;
}
