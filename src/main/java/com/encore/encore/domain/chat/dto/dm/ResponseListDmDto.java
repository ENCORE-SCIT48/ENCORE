package com.encore.encore.domain.chat.dto.dm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseListDmDto {
    private Long roomId;
    private Long otherProfileId;
    private String otherUserNickname;
    private String otherProfileMode;
    private String otherUserProfileImg;
    private String latestMessage;
    private LocalDateTime latestMessageAt;
    private String status;
}
