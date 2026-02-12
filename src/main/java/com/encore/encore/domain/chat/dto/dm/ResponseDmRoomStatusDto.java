package com.encore.encore.domain.chat.dto.dm;

import com.encore.encore.domain.chat.entity.ChatParticipant;
import com.encore.encore.domain.member.entity.ActiveMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * dm방 생성 후 participantStatus 를 응답하는 dto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDmRoomStatusDto {
    private Long roomId;
    private Long otherProfileId;
    private ActiveMode otherProfileMode;
    private ChatParticipant.ParticipantStatus myParticipantStatus;

}
