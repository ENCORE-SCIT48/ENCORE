package com.encore.encore.domain.performance.dto;

import com.encore.encore.domain.performance.entity.PerformanceCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 공연 등록/수정 요청 DTO.
 * - 공연자(아티스트)가 공연을 생성/수정할 때 사용하는 기본 필드만 포함합니다.
 */
@Getter
@Setter
@NoArgsConstructor
public class PerformanceCreateRequestDto {

    /** 공연 제목 */
    private String title;

    /** 공연 설명 */
    private String description;

    /** 공연 대표 이미지(포스터) URL - 파일 업로드 연동은 추후 확장 */
    private String performanceImageUrl;

    /** 장르 카테고리 (MUSICAL / PLAY / BAND) */
    private String category;

    /** 예상 관객 수(좌석 수 등) */
    private Integer capacity;

    /** 공연이 열리는 공연장 ID */
    private Long venueId;

    public PerformanceCategory toCategoryEnum() {
        if (category == null || category.isBlank()) {
            return null;
        }
        try {
            return PerformanceCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

