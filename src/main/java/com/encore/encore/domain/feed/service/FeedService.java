package com.encore.encore.domain.feed.service;

import com.encore.encore.domain.community.entity.Review;
import com.encore.encore.domain.community.repository.ReviewRepository;
import com.encore.encore.domain.feed.dto.FeedItemDto;
import com.encore.encore.domain.feed.dto.FeedResponseDto;
import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.domain.performance.entity.PerformanceSchedule;
import com.encore.encore.domain.performance.repository.PerformanceRepository;
import com.encore.encore.domain.performance.repository.PerformanceScheduleRepository;
import com.encore.encore.domain.performance.repository.UserPerformanceRelationRepository;
import com.encore.encore.domain.user.entity.RelationType;
import com.encore.encore.domain.user.repository.UserRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final PerformanceScheduleRepository performanceScheduleRepository;
    private final UserRelationRepository userRelationRepository;
    private final PerformanceRepository performanceRepository;
    private final ReviewRepository reviewRepository;
    private final UserPerformanceRelationRepository userPerformanceRelationRepository;

    /**
     * 게스트/빈 피드 공통으로 사용하는 HOT 공연 카드 생성.
     */
    private List<FeedItemDto> buildHotPerformances() {
        List<Performance> hotPerformances = performanceRepository
            .findTop10ByIsDeletedFalseOrderByCreatedAtDesc();

        List<FeedItemDto> items = new ArrayList<>();
        for (Performance p : hotPerformances) {
            items.add(FeedItemDto.builder()
                .type("HOT_PERFORMANCE")
                .performanceId(p.getPerformanceId())
                .title(p.getTitle())
                .performanceImageUrl(p.getPerformanceImageUrl())
                .startTime(null)
                .message("지금 인기 있는 공연이에요")
                .build());
        }
        return items;
    }

    // 시작 30분 전 알림
    private static final int NOTIFY_BEFORE_MINUTES = 30;

    // 관계 상태 문자열(프로젝트 enum화 전까지는 문자열로 일단 통일)
    private static final String STATUS_WISHED = "WISHED";
    private static final String STATUS_WATCHED = "WATCHED";

    public FeedResponseDto getFeed(Long loginUserId) {
        LocalDateTime now = LocalDateTime.now();

        List<FeedItemDto> result = new ArrayList<>();
        // 1) 내 활동 관련: 곧 시작하는 찜 공연 / 리뷰 리마인드
        result.addAll(buildUpcomingWished(now, loginUserId));
        result.addAll(buildReviewReminders(loginUserId));

        // 2) 관계 기반: 팔로우한 사람이 찜한 공연
        result.addAll(buildFollowWished(now, loginUserId));

        // 3) 전체 커뮤니티의 움직임: 최근 공연 후기 / 좌석 리뷰
        result.addAll(buildRecentPerformanceReviews());
        result.addAll(buildRecentSeatReviews());

        // 아직 피드가 비어 있으면 HOT 공연으로 채워서 최소한의 볼거리는 제공
        if (result.isEmpty()) {
            result.addAll(buildHotPerformances());
        }

        return FeedResponseDto.builder()
            .items(result)
            .build();
    }

    /**
     * 비로그인(게스트)용 피드.
     * - 로그인 정보가 없을 때 기본으로 보여줄 추천/랜덤 공연 피드를 구성한다.
     * - 현재는 단순히 "다가오는 찜한 공연" / "팔로우 기반" 없이 빈 리스트를 반환하고,
     *   추후 HOT 공연/랜덤 공연 기반으로 확장할 수 있다.
     */
    public FeedResponseDto getGuestFeed() {
        return FeedResponseDto.builder()
            // 삭제되지 않은 공연 중 최신순 Top10을 HOT 피드로 노출 (비로그인/테스트 데이터 대응)
            .items(buildHotPerformances())
            .build();
    }

    /**
     * [피드] 최근 공연 후기 카드 (좌석 리뷰 제외)
     */
    private List<FeedItemDto> buildRecentPerformanceReviews() {
        var page = reviewRepository
            .findBySeatIsNullAndIsDeletedFalseAndPerformance_IsDeletedFalseOrderByCreatedAtDesc(PageRequest.of(0, 5));

        List<FeedItemDto> items = new ArrayList<>();
        for (Review r : page.getContent()) {
            var performance = r.getPerformance();
            if (performance == null) continue;
            var user = r.getUser();

            String title = performance.getTitle();
            String content = r.getContent();
            String snippet = (content == null || content.isBlank())
                ? ""
                : (content.length() > 80 ? content.substring(0, 80) + "…" : content);

            items.add(FeedItemDto.builder()
                .type("RECENT_REVIEW")
                .performanceId(performance.getPerformanceId())
                .title(title)
                .performanceImageUrl(performance.getPerformanceImageUrl())
                .startTime(r.getCreatedAt())
                .actorUserId(user != null ? user.getUserId() : null)
                .actorNickname(user != null ? user.getNickname() : null)
                .rating(r.getRating())
                .message(snippet)
                .build());
        }
        return items;
    }

    /**
     * [피드] 최근 좌석 리뷰 카드
     */
    private List<FeedItemDto> buildRecentSeatReviews() {
        var page = reviewRepository
            .findBySeatIsNotNullAndIsDeletedFalseAndPerformance_IsDeletedFalseOrderByCreatedAtDesc(PageRequest.of(0, 5));

        List<FeedItemDto> items = new ArrayList<>();
        for (Review r : page.getContent()) {
            var performance = r.getPerformance();
            if (performance == null) continue;
            var user = r.getUser();
            var seat = r.getSeat();

            String title = performance.getTitle();
            String content = r.getContent();
            String snippet = (content == null || content.isBlank())
                ? ""
                : (content.length() > 80 ? content.substring(0, 80) + "…" : content);

            String seatLabel = null;
            if (seat != null) {
                String number = seat.getSeatNumber();
                Integer floor = seat.getSeatFloor();
                if (floor != null && number != null) {
                    seatLabel = floor + "층 " + number;
                } else if (number != null) {
                    seatLabel = number;
                }
            }

            items.add(FeedItemDto.builder()
                .type("RECENT_SEAT_REVIEW")
                .performanceId(performance.getPerformanceId())
                .title(title)
                .performanceImageUrl(performance.getPerformanceImageUrl())
                .startTime(r.getCreatedAt())
                .actorUserId(user != null ? user.getUserId() : null)
                .actorNickname(user != null ? user.getNickname() : null)
                .rating(r.getRating())
                .seatLabel(seatLabel)
                .message(snippet)
                .build());
        }
        return items;
    }

    private List<FeedItemDto> buildUpcomingWished(LocalDateTime now, Long loginUserId) {
        LocalDateTime to = now.plusMinutes(NOTIFY_BEFORE_MINUTES);

        List<PerformanceSchedule> schedules = performanceScheduleRepository.findUpcomingWishedSchedules(
            loginUserId,
            STATUS_WISHED,
            now,
            to
        );

        List<FeedItemDto> items = new ArrayList<>();
        for (PerformanceSchedule ps : schedules) {
            String title = ps.getPerformance().getTitle();

            items.add(FeedItemDto.builder()
                .type("UPCOMING_WISHED")
                .performanceId(ps.getPerformance().getPerformanceId())
                .title(title)
                .performanceImageUrl(ps.getPerformance().getPerformanceImageUrl())
                .startTime(ps.getStartTime())
                .notifyBeforeMinutes(NOTIFY_BEFORE_MINUTES)
                .message("공연 시작 " + NOTIFY_BEFORE_MINUTES + "분 전이에요")
                .build());
        }
        return items;
    }

    private List<FeedItemDto> buildFollowWished(LocalDateTime now, Long loginUserId) {
        List<Long> followedUserIds = userRelationRepository.findTargetIdsByActorAndRelationType(
            loginUserId,
            RelationType.FOLLOW
        );

        if (followedUserIds == null || followedUserIds.isEmpty()) {
            return List.of();
        }

        // 너무 많이 뜨면 UI 깨지니까 초기 버전은 10개 제한
        List<PerformanceScheduleRepository.FollowWishedRow> rows =
            performanceScheduleRepository.findFollowWishedSchedules(
                followedUserIds,
                STATUS_WISHED,
                now,
                PageRequest.of(0, 10)
            );

        List<FeedItemDto> items = new ArrayList<>();
        for (PerformanceScheduleRepository.FollowWishedRow row : rows) {
            items.add(FeedItemDto.builder()
                .type("FOLLOW_WISHED")
                .performanceId(row.getPerformanceId())
                .title(row.getTitle())
                .performanceImageUrl(row.getPerformanceImageUrl())
                .startTime(row.getStartTime())
                .actorUserId(row.getActorUserId())
                .actorNickname(row.getActorNickname())
                .message(row.getActorNickname() + "님이 찜한 공연")
                .build());
        }
        return items;
    }

    /**
     * [피드] 내가 본 공연 중 아직 리뷰를 남기지 않은 공연 리마인드 카드
     */
    private List<FeedItemDto> buildReviewReminders(Long loginUserId) {
        // 이미 공연 후기를 남긴 performanceId 목록
        List<Long> reviewedIds = reviewRepository.findReviewedPerformanceIdsByUser(loginUserId);
        Set<Long> reviewedSet = new HashSet<>(reviewedIds != null ? reviewedIds : List.of());

        // 최근 본 공연(WATCHED) 목록 중 상위 10개
        var page = userPerformanceRelationRepository
            .findPerformancesByUserIdAndStatusAndKeywordOrderByWatchedAtDesc(
                loginUserId,
                STATUS_WATCHED,
                null,
                PageRequest.of(0, 10)
            );

        List<FeedItemDto> items = new ArrayList<>();
        for (Performance p : page.getContent()) {
            if (p == null) continue;
            Long perfId = p.getPerformanceId();
            if (perfId == null) continue;
            if (reviewedSet.contains(perfId)) continue; // 이미 공연 후기를 남긴 경우 제외

            String title = p.getTitle();

            items.add(FeedItemDto.builder()
                .type("REVIEW_REMINDER")
                .performanceId(perfId)
                .title(title)
                .performanceImageUrl(p.getPerformanceImageUrl())
                .startTime(null) // 리마인드는 정렬 우선순위가 낮아도 되므로 시간은 비움
                .message("이 공연, 아직 후기를 남기지 않았어요")
                .build());

            // 리마인드 카드는 너무 많지 않게 5개까지만
            if (items.size() >= 5) break;
        }
        return items;
    }
}
