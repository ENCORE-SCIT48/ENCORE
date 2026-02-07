package com.encore.encore.domain.chat.dto;

import com.encore.encore.domain.chat.entity.ChatPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseParticipantChatPostDto {
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
     * 가장 최근에 활동이 일어난 시간을 기준으로 가져오기 채팅방을 가져오기 위한 dto
     *
     * @param chatPost
     * @return
     */
    public static ResponseParticipantChatPostDto from(ChatPost chatPost) {
        return ResponseParticipantChatPostDto.builder()
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
