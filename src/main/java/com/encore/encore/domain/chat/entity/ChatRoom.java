package com.encore.encore.domain.chat.entity;

import com.encore.encore.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "chat_room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "is_deleted = false")
public class ChatRoom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private ChatPost chatPost;
    @Column(nullable = false)
    private boolean isDeleted = false;
    @Enumerated(EnumType.STRING)
    private RoomType roomType;

    public enum RoomType {
        CHAT,
        DM
    }
}
