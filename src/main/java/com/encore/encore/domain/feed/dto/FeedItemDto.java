package com.encore.encore.domain.feed.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FeedItemDto {

    /**
     * 타입
     * - UPCOMING_WISHED : 내가 찜한 공연, 시작 임박 알림
     * - FOLLOW_WISHED   : 내가 팔로우한 사람이 찜한 공연
     * - RECENT_REVIEW   : 최근 공연 후기
     * - RECENT_SEAT_REVIEW : 최근 좌석 리뷰
     * - REVIEW_REMINDER : 내가 본 공연 중 아직 리뷰를 남기지 않은 공연 리마인드
     */
    private String type;

    private Long performanceId;
    private String title;

    /** 공연 포스터 URL (피드 카드 배경용) */
    private String performanceImageUrl;

    private LocalDateTime startTime;

    // FOLLOW_WISHED일 때만 채움
    private Long actorUserId;
    private String actorNickname;

    // UPCOMING_WISHED일 때만 채움
    private Integer notifyBeforeMinutes;

    // 리뷰/좌석 리뷰일 때만 채움
    private Integer rating;
    private String seatLabel;

    // 화면에 띄울 문구
    private String message;
}
