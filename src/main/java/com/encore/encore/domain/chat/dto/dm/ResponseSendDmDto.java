package com.encore.encore.domain.chat.dto.dm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseSendDmDto {
    private Long roomId;
    private Long messageId;
    private Long profileId;
    private String profileMode;
    private String senderName;
    private String content;
    private LocalDateTime createdAt;

}
