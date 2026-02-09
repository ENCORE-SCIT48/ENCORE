package com.encore.encore.domain.chat.dto;

import com.encore.encore.domain.chat.entity.ChatPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * [설명] 채팅 게시글 수정 요청 DTO
 * <p>
 * 클라이언트로부터 전달받는 채팅방 수정 요청 정보를 담는 객체입니다.
 * 수정 가능한 항목은 제목, 내용, 상태(Status)입니다.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestUpdateChatPostDto {
    private String title;
    private String content;
    private ChatPost.Status status;
}
