package com.encore.encore.domain.user.dto;

import com.encore.encore.domain.user.entity.TargetType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestBlockDto {
    private Long targetId;
    private TargetType targetType; // USER, VENUE, PERFORMANCE 등
    private String targetProfileMode; // USER일 때만 필수, 나머지는 null 가능
}
