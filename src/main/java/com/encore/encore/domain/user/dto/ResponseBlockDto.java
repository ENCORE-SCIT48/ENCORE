package com.encore.encore.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseBlockDto {
    private Long targetId;
    private String targetProfileMode;

    @JsonProperty("isBlocked")
    private boolean isBlocked; // 팔로우가 아니니 이름도 확실하게!
}
