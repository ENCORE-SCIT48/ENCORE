package com.encore.encore.domain.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseFollowListDto {
    private Long userId;
    private String userName;
    private String profileMode;
    private boolean isFollowing; // 로그인 사용자 기준

}
