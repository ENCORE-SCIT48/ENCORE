package com.encore.encore.domain.chat.dto;

import com.encore.encore.domain.chat.entity.ChatPost;
import com.encore.encore.domain.chat.entity.ChatRoom;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseDeleteChatPostDto {
    private Long postId;
    private String title;
    private Long chatRoomId;
    private boolean chatPostIsDeleted;
    private boolean chatRoomIsDeleted;

    // 엔티티 → DTO 변환용 정적 메서드
    public static ResponseDeleteChatPostDto from(ChatPost chatPost, ChatRoom chatRoom) {
        return ResponseDeleteChatPostDto.builder()
            .postId(chatPost.getId())
            .title(chatPost.getTitle())
            .chatRoomId(chatRoom.getRoomId())
            .chatPostIsDeleted(chatPost.isDeleted())
            .chatRoomIsDeleted(chatRoom.isDeleted())
            .build();
    }
}
