package com.encore.encore.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 채팅방 참여자 정보를 반환하는 DTO
 * <p>
 * 각 참여자의 기본 정보, 상태, 닉네임, 프로필 이미지 등을 포함합니다.
 * </p>
 * <p>예시 필드:
 * <ul>
 *     <li>participantId: 참여자 고유 ID</li>
 *     <li>activeMode: 참여자의 프로필 모드</li>
 *     <li>activeId: 참여자의 프로필 ID</li>
 *     <li>roomId: 참여자가 속한 채팅방 ID</li>
 *     <li>nickName: 참여자 닉네임</li>
 * </ul>
 * </p>
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseParticipantDto {
    private Long participantId;
    private String profileMode;
    private Long profileId;
    private Long roomId;
    private String nickName;
}
