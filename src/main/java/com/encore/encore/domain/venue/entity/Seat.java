package com.encore.encore.domain.venue.entity;

import com.encore.encore.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;
    private Integer seatFloor;
    private Integer xPos;        // 캔버스 X 좌표
    private Integer yPos;        // 캔버스 Y 좌표
    private Integer seatNumber;
    private String seatType;
}
