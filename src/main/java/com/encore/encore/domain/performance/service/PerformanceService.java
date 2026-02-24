package com.encore.encore.domain.performance.service;

import com.encore.encore.domain.community.entity.Review;
import com.encore.encore.domain.community.repository.ReviewRepository;
import com.encore.encore.domain.performance.dto.PerformanceDetailDto;
import com.encore.encore.domain.performance.dto.PerformanceListItemDto;
import com.encore.encore.domain.performance.dto.PerformanceReviewItemDto;
import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.domain.performance.repository.PerformanceRepository;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final ReviewRepository reviewRepository;

    /**
     * 공연 목록 조회 (검색/카테고리/페이징 지원)
     *
     * - category가 "전체" 또는 비어 있으면 - 제목 검색만 적용하거나(검색어 있을 때), 전체 조회
     * - category가 특정 값이면 status 필터를 적용하고, 검색어가 있으면 title도 함께 검색
     *
     * @param keyword 공연 제목 검색어(null/빈값 가능)
     * @param category 카테고리(전체/밴드/뮤지컬/연극 등, null/빈값 가능)
     * @param pageable 페이징 정보
     * @return 공연 목록 페이지(리스트 DTO)
     */
    public Page<PerformanceListItemDto> getPerformances(String keyword, String category, Pageable pageable) {

        boolean hasKeyword = StringUtils.hasText(keyword);
        boolean hasCategory = StringUtils.hasText(category) && !"전체".equals(category);

        log.info("[Performance] list request - keyword={}, category={}, page={}, size={}",
            keyword,
            category,
            pageable.getPageNumber(),
            pageable.getPageSize()
        );

        Page<Performance> performances;

        // 경우의 수를 명확히 나눠야 "전체 + 검색" 같은 케이스에서 검색이 무시되는 버그를 막을 수 있음
        if (hasCategory && hasKeyword) {
            // 카테고리 + 검색어
            performances = performanceRepository.findByTitleContainingIgnoreCaseAndStatus(keyword, category, pageable);
        } else if (hasCategory) {
            // 카테고리만
            performances = performanceRepository.findByStatus(category, pageable);
        } else if (hasKeyword) {
            // 전체 + 검색어
            performances = performanceRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        } else {
            // 전체
            performances = performanceRepository.findAll(pageable);
        }

        log.info("[Performance] list result - totalElements={}, totalPages={}",
            performances.getTotalElements(),
            performances.getTotalPages()
        );

        return performances.map(PerformanceListItemDto::new);
    }

    /**
     * 공연 상세 정보를 조회. 대상이 없으면 NOT_FOUND 예외를 발생.
     * @param performanceId 공연 ID
     * @return 공연 상세 DTO
     */
    public PerformanceDetailDto getPerformance(Long performanceId) {
        log.info("[Performance] detail request - performanceId={}", performanceId);

        Performance performance = performanceRepository.findDetailById(performanceId)
            .orElseThrow(() -> new ApiException(
                ErrorCode.NOT_FOUND,
                "공연을 찾을 수 없습니다. performanceId=" + performanceId
            ));

        log.info("[Performance] detail found - performanceId={}", performanceId);
        return new PerformanceDetailDto(performance);
    }

    /**
     * 핫한 공연 Top10을 조회. (임시 기준 - OPEN 상태 + createdAt 최신순)
     * @return 핫한 공연 리스트(리스트 DTO)
     */
    public List<PerformanceListItemDto> getHotPerformances() {
        log.info("[Performance] hot list request - status=OPEN, limit=10");

        List<PerformanceListItemDto> result = performanceRepository
            .findTop10ByStatusOrderByCreatedAtDesc("OPEN")
            .stream()
            .map(PerformanceListItemDto::new)
            .toList();

        log.info("[Performance] hot list result - size={}", result.size());
        return result;
    }

    /**
     * 공연에 대한 후기 목록 조회
     * - 좌석이 연결된 후기(Seat 연관이 존재하는 리뷰)는 제외하고 공연 후기만 조회 (seat IS NULL)
     * - 공연이 존재하지 않을 경우 NOT_FOUND 예외를 발생시킴
     * - 페이징 처리를 지원
     * - 정렬 기준을 지원
     *   - sort = "latest" (기본값) -> 작성일 최신순 (createdAt DESC)
     *   - sort = "rating" > 별점순 (rating DESC, createdAt DESC)
     *
     * @param performanceId 공연 ID
     * @param pageable 페이징 정보
     * @param sort 정렬 기준 (latest / rating)
     * @return 공연 후기 페이지(리스트 DTO)
     * @throws ApiException 공연이 존재하지 않을 경우
     */
    public Page<PerformanceReviewItemDto> getPerformanceReviews(Long performanceId, Pageable pageable, String sort) {

        // 리뷰 조회 요청 로그
        log.info("[Performance] reviews request - performanceId={}, sort={}, page={}, size={}",
            performanceId,
            sort,
            pageable.getPageNumber(),
            pageable.getPageSize()
        );

        if (!performanceRepository.existsById(performanceId)) {
            throw new ApiException(ErrorCode.NOT_FOUND, "공연을 찾을 수 없습니다. performanceId=" + performanceId);
        }

        Page<Review> page;

        if ("rating".equalsIgnoreCase(sort)) {
            page = reviewRepository
                .findByPerformance_PerformanceIdAndSeatIsNullAndIsDeletedFalseOrderByRatingDescCreatedAtDesc(
                    performanceId, pageable
                );
        } else {
            page = reviewRepository
                .findByPerformance_PerformanceIdAndSeatIsNullAndIsDeletedFalseOrderByCreatedAtDesc(
                    performanceId, pageable
                );
        }

        return page.map(review -> new PerformanceReviewItemDto(
            review.getReviewId(),
            review.getUser() != null ? review.getUser().getUserId() : 0L,
            review.getUser() != null ? review.getUser().getNickname() : "-",
            review.getRating(),
            review.getContent(),
            review.getCreatedAt()
        ));
    }

    public Map<String, Object> getPerformanceReviewSummary(Long performanceId) {

        Object[] row = reviewRepository.getPerformanceReviewSummary(performanceId);

        double avgRating = 0.0;
        long reviewCount = 0L;

        if (row != null && row.length > 0 && row[0] instanceof Object[] inner && inner.length >= 2) {
            Object avgObj = inner[0];
            Object cntObj = inner[1];

            avgRating = (avgObj == null) ? 0.0 : ((Number) avgObj).doubleValue();
            reviewCount = (cntObj == null) ? 0L : ((Number) cntObj).longValue();
        }

        return Map.of(
            "avgRating", avgRating,
            "reviewCount", reviewCount
        );
    }

    @Transactional
    public Long createPerformanceReview(Long performanceId, Long userId, Integer rating, String content) {

        if (rating == null || rating < 1 || rating > 5) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "별점은 1~5점이어야 합니다.");
        }

        String c = (content == null) ? "" : content.trim();
        if (c.length() < 5) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "리뷰는 5자 이상 입력해 주세요.");
        }

        Performance performance = performanceRepository.findById(performanceId)
            .orElseThrow(() -> new ApiException(
                ErrorCode.NOT_FOUND,
                "공연을 찾을 수 없습니다. performanceId=" + performanceId
            ));

        Review review = Review.builder()
            .performance(performance)
            .user(com.encore.encore.domain.user.entity.User.builder().userId(userId).build())
            .seat(null)
            .rating(rating)
            .content(c)
            .build();

        return reviewRepository.save(review).getReviewId();
    }

    @Transactional
    public Map<String, Object> getPerformanceReviewForEdit(Long performanceId, Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "리뷰를 찾을 수 없습니다."));

        if (review.getPerformance() == null || !performanceId.equals(review.getPerformance().getPerformanceId())) {
            throw new ApiException(ErrorCode.NOT_FOUND, "리뷰를 찾을 수 없습니다.");
        }

        if (review.getSeat() != null) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "좌석 리뷰는 이 API 대상이 아닙니다.");
        }

        if (review.getUser() == null || !userId.equals(review.getUser().getUserId())) {
            throw new ApiException(ErrorCode.FORBIDDEN, "수정 권한이 없습니다.");
        }

        return Map.of(
            "rating", review.getRating(),
            "content", review.getContent()
        );
    }

    @Transactional
    public void updatePerformanceReview(
        Long performanceId,
        Long reviewId,
        Long userId,
        Integer rating,
        String content
    ) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "별점은 1~5점이어야 합니다.");
        }

        String c = (content == null) ? "" : content.trim();
        if (c.length() < 5) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "리뷰는 5자 이상 입력해 주세요.");
        }

        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "리뷰를 찾을 수 없습니다. reviewId=" + reviewId));

        // 공연 리뷰 맞는지 검증
        if (!review.getPerformance().getPerformanceId().equals(performanceId)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "해당 공연의 리뷰가 아닙니다.");
        }

        // 내 리뷰인지 검증
        if (!review.getUser().getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "수정 권한이 없습니다.");
        }

        review.setRating(rating);
        review.setContent(c);
    }

    @Transactional
    public void deletePerformanceReview(Long performanceId, Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "리뷰를 찾을 수 없습니다. reviewId=" + reviewId));

        // 공연 매칭 체크
        if (review.getPerformance() == null || !review.getPerformance().getPerformanceId().equals(performanceId)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "공연에 속한 리뷰가 아닙니다.");
        }

        // 작성자 체크
        if (review.getUser() == null || !review.getUser().getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "삭제 권한이 없습니다.");
        }

        // 논리삭제
        review.delete(); // BaseEntity의 isDeleted=true
    }
}
