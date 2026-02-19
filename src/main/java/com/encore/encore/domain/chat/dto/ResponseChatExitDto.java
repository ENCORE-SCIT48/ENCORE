package com.encore.encore.domain.chat.dto;

import lombok.*;

/**
 * 채팅방 퇴장 결과를 반환하는 DTO
 * <p>
 * 사용 예시:
 * <ul>
 *     <li>채팅방 퇴장 성공: {@link #success(Long)}</li>
 *     <li>퇴장 금지 (글쓴이): {@link #forbidden(Long)}</li>
 * </ul>
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseChatExitDto {
    private Long roomId;
    private boolean exitSuccess;
    private boolean isOwner;
    private String message;

    public static ResponseChatExitDto success(Long roomId) {
        return ResponseChatExitDto.builder()
            .roomId(roomId)
            .exitSuccess(true)
            .isOwner(false)
            .message("채팅방을 나갔습니다.")
            .build();
    }

    public static ResponseChatExitDto forbidden(Long roomId) {
        return ResponseChatExitDto.builder()
            .roomId(roomId)
            .exitSuccess(false)
            .isOwner(true)
            .message("글쓴이는 퇴장할 수 없습니다.")
            .build();
    }
}
