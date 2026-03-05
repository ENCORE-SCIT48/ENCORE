package com.encore.encore.domain.venue.entity;

import com.encore.encore.domain.member.entity.HostProfile;
import com.encore.encore.domain.member.entity.PerformerProfile;
import com.encore.encore.global.common.BaseEntity;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 공연장 대관 예약 엔티티.
 * 대관은 공연(Performance)과 별개이며, 공연자가 필요할 때 공연장을 빌리는 개념이다.
 * 공연자가 호스트 소유 공연장에 대관을 신청하고, 호스트가 승낙/거절하는 라이프사이클을 관리한다.
 */
@Entity
@Table(name = "venue_reservation")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class VenueReservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long reservationId;

    /** 대관 신청 대상 공연장 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    /** 공연장 소유 호스트 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private HostProfile host;

    /** 대관 신청 공연자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performer_id", nullable = false)
    private PerformerProfile performer;

    /** 대관 시작 일시 */
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    /** 대관 종료 일시 */
    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    /** 예약 상태 (PENDING / APPROVED / REJECTED) */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    /** 공연자가 신청 시 남기는 메시지 */
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    /** 호스트가 거절 시 남기는 사유 */
    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    // ─── 도메인 메서드 ────────────────────────────────────────────

    /**
     * 호스트가 대관 요청을 승낙한다.
     * 상태가 PENDING 일 때만 가능하다.
     *
     * @throws ApiException PENDING 상태가 아닐 경우 (INVALID_REQUEST)
     */
    public void approve() {
        if (this.status != ReservationStatus.PENDING) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "대기 중인 예약만 승낙할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = ReservationStatus.APPROVED;
    }

    /**
     * 호스트가 대관 요청을 거절한다.
     * 상태가 PENDING 일 때만 가능하다.
     *
     * @param reason 거절 사유 (빈 문자열 허용, null 불가)
     * @throws ApiException PENDING 상태가 아닐 경우 (INVALID_REQUEST)
     * @throws ApiException reason 이 null 인 경우 (INVALID_REQUEST)
     */
    public void reject(String reason) {
        if (this.status != ReservationStatus.PENDING) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "대기 중인 예약만 거절할 수 있습니다. 현재 상태: " + this.status);
        }
        if (reason == null) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "거절 사유는 null 일 수 없습니다.");
        }
        this.status = ReservationStatus.REJECTED;
        this.rejectReason = reason;
    }
}
