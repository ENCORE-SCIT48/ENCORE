package com.encore.encore.domain.chat.dto;

import com.encore.encore.domain.chat.entity.ChatPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * [설명] 채팅 게시글 상세 정보 응답 DTO
 * <p>
 * 특정 채팅 게시글의 상세 정보를 클라이언트에게 전달할 때 사용하는 DTO입니다.
 * 게시글 작성자, 공연 정보, 모집 인원, 상태 등의 필드를 포함합니다.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDetailChatPostDto {
    private Long id;
    private Long performanceId;
    private String performanceTitle;
    private String writerName;
    private Long writerId;
    private String writerProfileMode;
    private String title;
    private String content;
    private String currentMember;
    private Integer maxMember;
    private LocalDateTime createdAt;
    private ChatPost.Status status;
}
