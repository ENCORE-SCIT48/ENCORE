package com.encore.encore.domain.venue.entity;

import com.encore.encore.domain.member.entity.HostProfile;
import com.encore.encore.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "venue")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Venue extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long venueId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private HostProfile host;

    @Column(nullable = false)
    private String venueName;  // 공연장 이름

    @Column(nullable = false)
    private String address;    // 상세 주소

    private String contact;    // 연락처
    private String description; // 시설 소개
    private String venueImage; // 공연장 대표 사진 경로

    private String venueType;  // 공연장 유형 (소극장, 홀 등)
    private Integer totalSeats; // 총 좌석 수

    // 운영 및 가격 정보 (3단계 반영)
    private String openTime;    // 개장 시간 (HH:mm)
    private String closeTime;   // 폐장 시간 (HH:mm)
    private Integer bookingUnit; // 예약 단위 (분) - 예: 30, 60
    private Integer rentalFee;     // 시간 별 대관료

    private String facilities; //부대시설

    private String regularClosing;   // "MONDAY,SUNDAY"
    @Column(columnDefinition = "TEXT")
    private String temporaryClosing; // "2026-03-01,2026-05-05"
    /**
     * [설명] 공연장 정보를 일괄 업데이트합니다. (Dirty Checking 활용)
     */
    public void updateVenueInfo(String venueName, String address, String contact,
                                String description, String venueType, String venueImage,
                                String openTime, String closeTime, Integer bookingUnit,
                                Integer rentalFee, String facilities, Integer totalSeats,
                                String regularClosing, String temporaryClosing) {
        this.venueName = venueName;
        this.address = address;
        this.contact = contact;
        this.description = description;
        this.venueType = venueType;

        // 이미지는 새 경로가 들어왔을 때만 교체 (Null 방어)
        if (venueImage != null && !venueImage.isEmpty()) {
            this.venueImage = venueImage;
        }

        this.openTime = openTime;
        this.closeTime = closeTime;
        this.bookingUnit = bookingUnit;
        this.rentalFee = rentalFee;
        this.facilities = facilities;
        this.totalSeats = totalSeats;
        this.regularClosing = regularClosing;
        this.temporaryClosing = temporaryClosing;
    }

}
