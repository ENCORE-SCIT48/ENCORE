package com.encore.encore.domain.chat.dto;

import com.encore.encore.domain.chat.entity.ChatPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * [설명] 채팅방 게시글 수정 응답 DTO
 * <p>
 * 클라이언트에게 수정된 채팅방 게시글 정보를 전달하기 위한 데이터 구조입니다.
 * 수정된 게시글의 ID, 제목, 내용, 상태, 마지막 수정 시간을 포함합니다.
 */
@Getter
@Builder
@AllArgsConstructor
public class ResponseUpdateChatPostDto {
    private Long chatId;
    private String title;
    private String content;
    private String status;
    private LocalDateTime updatedAt;

    /**
     * [설명] 엔티티 → DTO 변환용 정적 메서드
     * <p>
     * ChatPost 엔티티를 받아 ResponseUpdateChatPostDto로 변환합니다.
     * 게시글 수정 후 최신 정보를 클라이언트에 전달할 때 사용됩니다.
     *
     * @param chatPost 변환 대상 ChatPost 엔티티
     * @return 변환된 ResponseUpdateChatPostDto 객체
     */
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
