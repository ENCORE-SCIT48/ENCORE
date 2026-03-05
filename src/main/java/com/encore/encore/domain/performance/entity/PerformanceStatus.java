package com.encore.encore.domain.performance.entity;

public enum PerformanceStatus {
    /** 등록만 된 상태 (공연 일정 전) */
    UPCOMING,
    /** 현재 진행중인 공연 */
    ONGOING,
    /** 종료된 공연 */
    ENDED,
    /** 취소된 공연 */
    CANCELLED
}

