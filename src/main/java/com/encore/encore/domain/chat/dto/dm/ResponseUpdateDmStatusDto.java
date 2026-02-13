package com.encore.encore.domain.chat.dto.dm;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
    DM상태 변경 결과 응답 객체
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseUpdateDmStatusDto {
    private Long roomId;
    private String status;
    private Long profileId;
    private String profileMode;
}
