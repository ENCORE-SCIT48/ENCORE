package com.encore.encore.domain.chat.dto;

import com.encore.encore.domain.chat.entity.ChatPost;
import com.encore.encore.domain.chat.entity.ChatPostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 공연별 채팅방 목록 조회 응답 DTO.
 * <p>
 * 목록에서 채팅 유형(후기/택시/뒤풀이 등)을 {@link #postType}, {@link #postTypeDisplayName}으로 노출합니다.
 * </p>
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseListChatPostDto {
    private Long id;
    private String title;
    private String status;
    private int currentMember;
    private int maxMember;
    private String updatedAt;
    /** 채팅 유형 코드 (REVIEW, TAXI_SHARE, AFTER_PARTY, GENERAL) */
    private String postType;
    /** 화면 표시용 한글 라벨 (후기·감상, 택시 동승, 뒤풀이, 일반) */
    private String postTypeDisplayName;

    /**
     * JPQL constructor projection용 생성자.
     * postType이 null이면 GENERAL로 간주하여 displayName 설정.
     */
    public ResponseListChatPostDto(Long id, String title, ChatPost.Status status,
                                   int currentMember, int maxMember, LocalDateTime updatedAt,
                                   ChatPostType postType) {
        this.id = id;
        this.title = title;
        this.status = status.name();
        this.currentMember = currentMember;
        this.maxMember = maxMember;
        this.updatedAt = updatedAt != null ? updatedAt.format(DateTimeFormatter.ofPattern("MM.dd HH:mm")) : null;
        this.postType = postType != null ? postType.name() : ChatPostType.GENERAL.name();
        this.postTypeDisplayName = postType != null ? postType.getDisplayName() : ChatPostType.GENERAL.getDisplayName();
    }
}
