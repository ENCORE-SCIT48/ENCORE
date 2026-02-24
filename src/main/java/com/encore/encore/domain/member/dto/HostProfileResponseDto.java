package com.encore.encore.domain.member.dto;

import com.encore.encore.domain.member.entity.HostProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class HostProfileResponseDto {
    private String organizationName;
    private String representativeName;
    private String businessNumber;
    private String openingDate;
    private String contactNumber;
    private String businessEmail;
    private String profileImageUrl;
    private boolean isVerified;
    private boolean isInitialized;

    public static HostProfileResponseDto from(HostProfile entity) {
        return HostProfileResponseDto.builder()
            .organizationName(entity.getOrganizationName())
            .representativeName(entity.getRepresentativeName())
            .businessNumber(entity.getBusinessNumber())
            .openingDate(entity.getOpeningDate())
            .contactNumber(entity.getContactNumber())
            .businessEmail(entity.getBusinessEmail())
            .profileImageUrl(entity.getProfileImageUrl())
            .isVerified(entity.isVerified())
            .isInitialized(entity.isInitialized())
            .build();
    }
}
