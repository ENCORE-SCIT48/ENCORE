package com.encore.encore.domain.chat.dto.dm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDmDto {
    private Long targetProfileId;
    private String targetProfileMode;
}
