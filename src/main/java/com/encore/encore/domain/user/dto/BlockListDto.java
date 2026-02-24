package com.encore.encore.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 차단 목록 출력용 DTO
 */
@Getter
@Builder
public class BlockListDto {
    private Long targetId;
    private String targetType;         // "USER", "VENUE" (JS 필터링용)
    private String targetProfileMode;
    private String name;               // 화면에 표시될 닉네임 또는 이름
    private String typeDisplayName;    // 화면에 표시될 한글 타입명 (예: 유저, 공연장)
}
