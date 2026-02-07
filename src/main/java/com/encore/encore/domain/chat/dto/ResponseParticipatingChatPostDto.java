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
public class ResponseParticipatingChatPostDto {
    private Long id;
    private String title;
    private ChatPost.Status status;
    private Integer currentMember;
    private Integer maxMember;
    private LocalDateTime updatedAt;
    private Long performanceId;
    private String performanceTitle;

    public static ResponseParticipatingChatPostDto from(ChatPost chatPost) {
        return ResponseParticipatingChatPostDto.builder()
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
