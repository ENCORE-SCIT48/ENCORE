package com.encore.encore.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/**
 * 채팅 메시지 응답 DTO
 * <p>
 * 채팅 메시지 상세 정보를 프론트에 전달하기 위한 객체입니다.
 * 메시지 ID, 발신자 프로필 ID, 발신자 이름, 내용, 생성 일시를 포함합니다.
 * </p>
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseChatMessage {
    private Long messageId;
    private Long profileId;
    private String profileMode;
    private String senderName;
    private String content;
    private LocalDateTime createdAt;
    private boolean isMine;


}

