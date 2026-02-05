package com.encore.encore.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatPostCreateRequestDTO {
    private Long performanceId;
    private String title;
    private String content;
    private Integer maxMember;
}


