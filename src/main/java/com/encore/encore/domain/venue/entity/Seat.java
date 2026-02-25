package com.encore.encore.domain.venue.entity;

import com.encore.encore.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seat")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Seat extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    private Integer seatFloor; // 층 (1, 2, 3...)

    // 에디터 캔버스의 좌표값
    private Integer xPos;
    private Integer yPos;

    // "A-1" 같은 형식을 저장하기 위해 String 유지
    private String seatNumber;

    // "vip", "r", "s", "a" 등급 저장
    private String seatType;
}
