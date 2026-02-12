package com.encore.encore.domain.chat.dto.dm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestSendDmDto {
    private Long roomId;
    private String content;
}
