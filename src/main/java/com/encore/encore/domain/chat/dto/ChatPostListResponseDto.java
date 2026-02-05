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
public class ChatPostListResponseDto {

    private Long id;
    private String title;
    private Integer currentMember;
    private Integer maxMember;
    public ChatPost.Status status;
}
