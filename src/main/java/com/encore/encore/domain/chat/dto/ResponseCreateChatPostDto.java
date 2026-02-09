package com.encore.encore.domain.chat.dto;

import com.encore.encore.domain.chat.entity.ChatPost;
import com.encore.encore.domain.chat.entity.ChatRoom;
import lombok.*;

/**
 * [설명] 채팅 게시글 생성 응답 DTO
 * <p>
 * 클라이언트에게 채팅방 생성 결과를 반환할 때 사용하는 객체입니다.
 * 채팅 게시글 정보와 해당 게시글에 연결된 채팅방 ID를 포함합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseCreateChatPostDto {
    private Long postId;
    private String title;
    private String content;
    private Integer maxMember;
    private Integer currentMember;
    private String status;
    private Long chatRoomId;

    // 엔티티 → DTO 변환용 정적 메서드
    public static ResponseCreateChatPostDto from(ChatPost chatPost, ChatRoom chatRoom) {
        return ResponseCreateChatPostDto.builder()
            .postId(chatPost.getId())
            .title(chatPost.getTitle())
            .content(chatPost.getContent())
            .maxMember(chatPost.getMaxMember())
            .currentMember(chatPost.getCurrentMember())
            .status(chatPost.getStatus().name())
            .chatRoomId(chatRoom.getRoomId())
            .build();
    }
}
