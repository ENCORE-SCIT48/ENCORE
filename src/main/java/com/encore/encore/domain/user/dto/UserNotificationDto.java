package com.encore.encore.domain.user.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotificationDto {
    private Boolean performanceStartAlert;
    private Boolean dmAlert;
}
