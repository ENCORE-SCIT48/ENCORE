package com.encore.encore.domain.chat.dto.dm;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DM 수락/거절
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDmStatusDto {
    private String status;
}
