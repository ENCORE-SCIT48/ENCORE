package com.encore.encore.domain.chat.entity;

import com.encore.encore.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

/**
 * 채팅방 엔티티. 게시글({@link ChatPost})과 1:1로 연결된다.
 * <p>
 * {@link RoomType#PERFORMANCE_ALL}: 해당 공연의 "전체 톡방"(공연 하나당 하나).
 * {@link RoomType#CHAT}: 개별 모집글(후기/택시/뒤풀이 등)에 대한 채팅방.
 * {@link RoomType#DM}: 1:1 DM 방.
 * </p>
 */
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
    @JoinColumn(name = "post_id")
    private ChatPost chatPost;
    @Enumerated(EnumType.STRING)
    private RoomType roomType;

    /** 채팅방 종류: 개별 모집방(CHAT), DM(DM), 공연 전체 톡방(PERFORMANCE_ALL) */
    public enum RoomType {
        CHAT,
        DM,
        PERFORMANCE_ALL
    }
}
