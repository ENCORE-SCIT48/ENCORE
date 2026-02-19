package com.encore.encore.domain.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileInfoDto {
    Long profileId;
    String profileMode;
    String profileName;
    private String profileImageUrl;
}
