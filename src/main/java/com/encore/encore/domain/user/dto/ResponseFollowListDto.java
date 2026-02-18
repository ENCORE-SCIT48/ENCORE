package com.encore.encore.domain.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseFollowListDto {
    private String userName;
    private Long profileId;
    private String profileMode;
    private boolean isFollowing; // 로그인 사용자 기준

}
