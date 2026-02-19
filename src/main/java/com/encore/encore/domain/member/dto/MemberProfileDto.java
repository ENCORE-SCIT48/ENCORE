package com.encore.encore.domain.member.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberProfileDto {
    private Long profileId;
    private String profileMode;
    private String userName;
    private String profileImg;
    private String userInfo;

    private int followingCount;
    private int followerCount;
}
