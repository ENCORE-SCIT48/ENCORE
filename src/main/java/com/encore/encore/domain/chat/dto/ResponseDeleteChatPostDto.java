package com.encore.encore.domain.chat.dto;

import com.encore.encore.domain.chat.entity.ChatPost;
import com.encore.encore.domain.chat.entity.ChatRoom;
import lombok.*;


/**
 * [설명] 채팅 게시글 삭제 응답 DTO
 * <p>
 * 클라이언트에게 채팅 게시글 삭제 처리 결과를 반환할 때 사용하는 객체입니다.
 * 게시글과 채팅방 삭제 여부를 포함합니다.
 */
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
