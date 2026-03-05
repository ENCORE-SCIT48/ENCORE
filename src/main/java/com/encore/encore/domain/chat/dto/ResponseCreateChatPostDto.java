package com.encore.encore.domain.chat.dto;

import com.encore.encore.domain.chat.entity.ChatPost;
import com.encore.encore.domain.chat.entity.ChatPostType;
import com.encore.encore.domain.chat.entity.ChatRoom;
import lombok.*;

/**
 * 채팅 게시글 생성 완료 시 반환하는 DTO.
 * <p>
 * 생성된 게시글 ID, 채팅방 ID, 제목·유형·상태 등 클라이언트에서 바로 표시할 수 있는 정보를 담습니다.
 * </p>
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
    private String postType;
    private String postTypeDisplayName;

    /** 엔티티 → DTO 변환. postType null 시 GENERAL로 표시 */
    public static ResponseCreateChatPostDto from(ChatPost chatPost, ChatRoom chatRoom) {
        ChatPostType type = chatPost.getPostType() != null ? chatPost.getPostType() : ChatPostType.GENERAL;
        return ResponseCreateChatPostDto.builder()
            .postId(chatPost.getId())
            .title(chatPost.getTitle())
            .content(chatPost.getContent())
            .maxMember(chatPost.getMaxMember())
            .currentMember(chatPost.getCurrentMember())
            .status(chatPost.getStatus().name())
            .chatRoomId(chatRoom.getRoomId())
            .postType(type.name())
            .postTypeDisplayName(type.getDisplayName())
            .build();
    }
}
