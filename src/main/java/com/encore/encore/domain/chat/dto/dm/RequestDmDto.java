package com.encore.encore.domain.chat.dto.dm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DM 방 생성 요청 dto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDmDto {
    private Long targetProfileId;
    private String targetProfileMode;
}
