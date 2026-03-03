package com.encore.encore.domain.venue.entity;

/**
 * 대관 예약 상태를 나타내는 Enum.
 * PENDING  : 신청 완료, 호스트 검토 대기
 * APPROVED : 호스트 승낙
 * REJECTED : 호스트 거절
 */
public enum ReservationStatus {
    PENDING,
    APPROVED,
    REJECTED
}
