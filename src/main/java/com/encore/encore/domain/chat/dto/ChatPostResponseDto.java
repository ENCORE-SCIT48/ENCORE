package com.encore.encore.domain.chat.dto;

import com.encore.encore.domain.chat.entity.ChatPost;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatPostResponseDto {
    private Long postId;
    private String title;
    private String content;
    private Integer maxMember;
    private Integer currentMember;
    private String status;

    // 엔티티 → DTO 변환용 정적 메서드
    public static ChatPostResponseDto from(ChatPost chatPost) {
        return ChatPostResponseDto.builder()
            .postId(chatPost.getId())
            .title(chatPost.getTitle())
            .content(chatPost.getContent())
            .maxMember(chatPost.getMaxMember())
            .currentMember(chatPost.getCurrentMember())
            .status(chatPost.getStatus().name())
            .build();
    }
}
