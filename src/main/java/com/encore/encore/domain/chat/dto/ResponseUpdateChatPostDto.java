package com.encore.encore.domain.chat.dto;

import com.encore.encore.domain.chat.entity.ChatPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ResponseUpdateChatPostDto {
    private Long chatId;
    private String title;
    private String content;
    private String status;
    private LocalDateTime updatedAt;

    // 엔티티를 DTO로 변환하는 정적 메서드
    public static ResponseUpdateChatPostDto from(ChatPost chatPost) {
        return ResponseUpdateChatPostDto.builder()
            .chatId(chatPost.getId())
            .title(chatPost.getTitle())
            .content(chatPost.getContent())
            .status(chatPost.getStatus().name())
            .updatedAt(chatPost.getUpdatedAt()) // BaseEntity의 수정 시각
            .build();
    }
}
