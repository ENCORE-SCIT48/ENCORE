package com.encore.encore.domain.chat.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatPostCreateRequestDto {

    private Long performanceId;
    @NotBlank(message = "제목은 필수 입니다.")
    @Size(min = 3, max = 100, message = "제목은 3자 이상 100자 이하로 입력해야 합니다.")
    private String title;
    private String content;
    @NotNull(message = "모집인원은 필수 입니다.")
    @Min(value = 2, message = "모집인원은 2명 이상으로 입력해야 합니다.")
    @Max(value = 50, message = "모집인원은 50명 이하여야 합니다.")
    private Integer maxMember;

    private Long hostId;
    private Long profileId;
    private Long performerId;

}


