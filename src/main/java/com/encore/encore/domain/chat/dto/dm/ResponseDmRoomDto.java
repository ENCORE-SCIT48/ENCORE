package com.encore.encore.domain.chat.dto.dm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDmRoomDto {
    private Long roomId;
    private String otherNickname;
    private Long otherProfileId;
    private String otherProfileMode;
    private String otherProfileImg;
}
