package com.encore.encore.domain.feed.service;

import com.encore.encore.domain.feed.dto.FeedItemDto;
import com.encore.encore.domain.feed.dto.FeedResponseDto;
import com.encore.encore.domain.performance.entity.PerformanceSchedule;
import com.encore.encore.domain.performance.repository.PerformanceScheduleRepository;
import com.encore.encore.domain.user.entity.RelationType;
import com.encore.encore.domain.user.repository.UserRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final PerformanceScheduleRepository performanceScheduleRepository;
    private final UserRelationRepository userRelationRepository;

    // 시작 30분 전 알림
    private static final int NOTIFY_BEFORE_MINUTES = 30;

    // 관계 상태 문자열(프로젝트 enum화 전까지는 문자열로 일단 통일)
    private static final String STATUS_WISHED = "WISHED";

    public FeedResponseDto getFeed(Long loginUserId) {
        LocalDateTime now = LocalDateTime.now();

        List<FeedItemDto> result = new ArrayList<>();
        result.addAll(buildUpcomingWished(now, loginUserId));
        result.addAll(buildFollowWished(now, loginUserId));

        return FeedResponseDto.builder()
            .items(result)
            .build();
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
                .startTime(row.getStartTime())
                .actorUserId(row.getActorUserId())
                .actorNickname(row.getActorNickname())
                .message(row.getActorNickname() + "님이 찜한 공연")
                .build());
        }
        return items;
    }
}
