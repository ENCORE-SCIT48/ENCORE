package com.encore.encore.domain.chat.dto.dm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * dm 글쓰기 요청 dto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestSendDmDto {
    private Long roomId;
    private String content;
}
