package com.encore.encore.domain.chat.dto;

import com.encore.encore.domain.chat.entity.ChatPost;
import com.encore.encore.domain.chat.entity.ChatPostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 참여 중인 채팅방 조회 응답 DTO.
 * <p>
 * 참여 채팅방 목록·핫 채팅 등에서 사용. 공연 정보와 채팅 유형(후기/택시/뒤풀이 등)을 포함합니다.
 * </p>
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseMyChatPostDto {
    private Long id;
    private String title;
    private String status;
    private Integer currentMember;
    private Integer maxMember;
    private LocalDateTime updatedAt;
    private Long performanceId;
    private String performanceTitle;
    private String roomType;
    private String postType;
    private String postTypeDisplayName;

    /** 엔티티 → DTO. postType null 시 GENERAL */
    public static ResponseMyChatPostDto from(ChatPost chatPost) {
        ChatPostType type = chatPost.getPostType() != null ? chatPost.getPostType() : ChatPostType.GENERAL;
        return ResponseMyChatPostDto.builder()
            .id(chatPost.getId())
            .title(chatPost.getTitle())
            .status(chatPost.getStatus().name())
            .currentMember(chatPost.getCurrentMember())
            .maxMember(chatPost.getMaxMember())
            .updatedAt(chatPost.getUpdatedAt())
            .performanceId(chatPost.getPerformance() != null ? chatPost.getPerformance().getPerformanceId() : null)
            .performanceTitle(chatPost.getPerformance() != null ? chatPost.getPerformance().getTitle() : "삭제된 공연")
            .postType(type.name())
            .postTypeDisplayName(type.getDisplayName())
            .build();
    }
}
