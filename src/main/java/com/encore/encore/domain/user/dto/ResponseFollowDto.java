package com.encore.encore.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseFollowDto {
    private Long targetId;          // 대상 유저 ID
    private String targetProfileMode;

    @JsonProperty("isFollowing")
    private boolean isFollowing;    // 현재 로그인 유저 기준 팔로우 상태
}
