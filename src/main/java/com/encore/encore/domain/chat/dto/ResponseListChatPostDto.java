package com.encore.encore.domain.chat.dto;

import com.encore.encore.domain.chat.entity.ChatPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * [설명] 채팅방 목록 조회를 위한 응답 DTO
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseListChatPostDto {
    private Long id;
    private String title;
    private String status;       // OPEN / CLOSED
    private int currentMember;
    private int maxMember;
    private String updatedAt;

    /**
     * [설명] JPQL Projection용 생성자
     */
    public ResponseListChatPostDto(Long id, String title, ChatPost.Status status,
                                   int currentMember, int maxMember, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.status = status.name(); // enum → String
        this.currentMember = currentMember;
        this.maxMember = maxMember;
        this.updatedAt = updatedAt.format(DateTimeFormatter.ofPattern("MM.dd HH:mm"));
    }
}
