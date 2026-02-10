package com.encore.encore.domain.chat.dto;

import com.encore.encore.domain.chat.entity.ChatPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * [설명] 참여 중인 채팅방 조회 응답 DTO
 * <p>
 * 로그인한 사용자가 참여하고 있는 채팅방 정보를 클라이언트에 전달하기 위한 데이터 구조입니다.
 * 채팅방 ID, 제목, 상태, 참여 인원, 최대 모집 인원, 마지막 업데이트 시간, 공연 정보 등을 포함합니다.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseMyChatPostDto {
    private Long id;
    private String title;
    private ChatPost.Status status;
    private Integer currentMember;
    private Integer maxMember;
    private LocalDateTime updatedAt;
    private Long performanceId;
    private String performanceTitle;
    private String roomType;

    /**
     * [설명] 엔티티 → DTO 변환용 정적 메서드
     * <p>
     * ChatPost 엔티티를 받아 DTO로 변환합니다.
     * 최근 활동 시간 기준으로 채팅방 정보를 가져올 때 사용됩니다.
     *
     * @param chatPost 변환 대상 ChatPost 엔티티
     * @return 변환된 ResponseParticipantChatPostDto 객체
     */
    public static ResponseMyChatPostDto from(ChatPost chatPost) {
        return ResponseMyChatPostDto.builder()
            .id(chatPost.getId())
            .title(chatPost.getTitle())
            .status(chatPost.getStatus())
            .currentMember(chatPost.getCurrentMember())
            .maxMember(chatPost.getMaxMember())
            .updatedAt(chatPost.getUpdatedAt())
            .performanceId(chatPost.getPerformance() != null ? chatPost.getPerformance().getPerformanceId() : null)
            .performanceTitle(chatPost.getPerformance() != null ? chatPost.getPerformance().getTitle() : "삭제된 공연")
            .build();
    }
}
