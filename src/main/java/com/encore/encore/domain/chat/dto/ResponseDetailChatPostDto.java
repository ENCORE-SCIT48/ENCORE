package com.encore.encore.domain.chat.dto;

import com.encore.encore.domain.chat.entity.ChatPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDetailChatPostDto {
    private Long id;
    private Long performanceId;
    private String performanceTitle;
    private Long writeProfileId;
    private String writerName;
    private String title;
    private String content;
    private String currentMember;
    private Integer maxMember;
    private ChatPost.Status status;
}
