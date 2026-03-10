package com.encore.encore.domain.performance.service;

import com.encore.encore.domain.community.entity.Review;
import com.encore.encore.domain.community.repository.ReviewRepository;
import com.encore.encore.domain.member.entity.PerformerProfile;
import com.encore.encore.domain.member.repository.PerformerProfileRepository;
import com.encore.encore.domain.performance.dto.PerformanceCreateRequestDto;
import com.encore.encore.domain.performance.dto.PerformanceDetailDto;
import com.encore.encore.domain.performance.dto.PerformanceListItemDto;
import com.encore.encore.domain.performance.dto.PerformanceReviewItemDto;
import com.encore.encore.domain.performance.dto.SeatOptionDto;
import com.encore.encore.domain.performance.dto.SeatReviewItemDto;
import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.domain.performance.entity.PerformanceCategory;
import com.encore.encore.domain.performance.entity.PerformanceRecruitStatus;
import com.encore.encore.domain.performance.entity.PerformanceStatus;
import com.encore.encore.domain.performance.repository.PerformanceRepository;
import com.encore.encore.domain.user.entity.User;
import com.encore.encore.domain.venue.entity.Seat;
import com.encore.encore.domain.venue.entity.Venue;
import com.encore.encore.domain.venue.repository.SeatRepository;
import com.encore.encore.domain.venue.repository.VenueRepository;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final ReviewRepository reviewRepository;
    private final SeatRepository seatRepository;
    private final PerformerProfileRepository performerProfileRepository;
    private final VenueRepository venueRepository;

    /**
     * 공연 목록 조회 (검색/카테고리/페이징 지원)
     * venueId가 있으면 해당 공연장에서 열리는 공연만 조회(공연장 상세·좌석 리뷰용).
     *
     * - category가 "전체" 또는 비어 있으면 - 제목 검색만 적용하거나(검색어 있을 때), 전체 조회
     * - category가 특정 값이면 status 필터를 적용하고, 검색어가 있으면 title도 함께 검색
     *
     * @param keyword    공연 제목 검색어(null/빈값 가능)
     * @param category   카테고리(전체/밴드/뮤지컬/연극 등, null/빈값 가능)
     * @param venueId    공연장 ID(있으면 해당 공연장 공연만, null이면 무시)
     * @param pageable   페이징 정보
     * @return 공연 목록 페이지(리스트 DTO)
     */
    public Page<PerformanceListItemDto> getPerformances(
        String keyword,
        String category,
        Long venueId,
        Pageable pageable
    ) {
        boolean hasKeyword = StringUtils.hasText(keyword);
        boolean hasCategory = StringUtils.hasText(category) && !"전체".equals(category);

        log.info("[Performance] list request - keyword={}, category={}, venueId={}, page={}, size={}",
            keyword,
            category,
            venueId,
            pageable.getPageNumber(),
            pageable.getPageSize()
        );

        Page<Performance> performances;
        PerformanceCategory categoryEnum = null;
        if (hasCategory) {
            try {
                categoryEnum = PerformanceCategory.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("[Performance] invalid category value - category={}", category, e);
            }
        }

        // 공연장 기준 조회 (공연장 상세에서 좌석 리뷰용) — is_deleted=false
        if (venueId != null) {
            performances =
                performanceRepository.findByVenue_VenueIdAndIsDeletedFalseOrderByCreatedAtDesc(venueId, pageable);
        } else if (categoryEnum != null && hasKeyword) {
            performances = performanceRepository.findByTitleContainingIgnoreCaseAndCategoryAndIsDeletedFalse(keyword, categoryEnum, pageable);
        } else if (categoryEnum != null) {
            performances = performanceRepository.findByCategoryAndIsDeletedFalse(categoryEnum, pageable);
        } else if (hasKeyword) {
            performances = performanceRepository.findByTitleContainingIgnoreCaseAndIsDeletedFalse(keyword, pageable);
        } else {
            performances = performanceRepository.findByIsDeletedFalse(pageable);
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
     * 핫한 공연 Top10을 조회. (임시 기준 - recruitStatus=OPEN + createdAt 최신순)
     * @return 핫한 공연 리스트(리스트 DTO)
     */
    public List<PerformanceListItemDto> getHotPerformances() {
        log.info("[Performance] hot list request - recruitStatus=OPEN, limit=10");

        List<PerformanceListItemDto> result = performanceRepository
            .findTop10ByRecruitStatusAndIsDeletedFalseOrderByCreatedAtDesc(PerformanceRecruitStatus.OPEN)
            .stream()
            .map(PerformanceListItemDto::new)
            .toList();

        log.info("[Performance] hot list result - size={}", result.size());
        return result;
    }

    /**
     * 공연을 새로 등록합니다.
     * - 공연자(PerformerProfile)를 creator로 설정합니다.
     * - 공연장(Venue)을 필수로 연결합니다.
     * - recruitStatus는 기본적으로 OPEN, status는 UPCOMING으로 설정합니다.
     */
    @Transactional
    public Long createPerformance(PerformanceCreateRequestDto dto, User user) {
        if (user == null || user.getUserId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        if (!StringUtils.hasText(dto.getTitle())) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "공연 제목은 필수입니다.");
        }

        if (dto.getVenueId() == null) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "공연장을 선택해 주세요.");
        }

        // 공연자 프로필 조회
        PerformerProfile performer = performerProfileRepository.findByUser(user)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "공연자 프로필을 찾을 수 없습니다."));

        // 공연장 조회
        Venue venue = venueRepository.findByVenueIdAndIsDeletedFalse(dto.getVenueId())
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "공연장을 찾을 수 없습니다. venueId=" + dto.getVenueId()));

        PerformanceCategory categoryEnum = dto.toCategoryEnum();

        Performance performance = Performance.builder()
            .title(dto.getTitle())
            .description(dto.getDescription())
            .performanceImageUrl(dto.getPerformanceImageUrl())
            .category(categoryEnum)
            .status(PerformanceStatus.UPCOMING)
            .recruitStatus(PerformanceRecruitStatus.OPEN)
            .capacity(dto.getCapacity())
            .venue(venue)
            .hostCreator(venue.getHost())
            .performerCreator(performer)
            .build();

        Performance saved = performanceRepository.save(performance);
        log.info("[Performance] created - performanceId={}, title={}", saved.getPerformanceId(), saved.getTitle());
        return saved.getPerformanceId();
    }

    /**
     * 공연 정보를 수정합니다.
     * - 본인이 creator인 공연자만 수정할 수 있습니다.
     */
    @Transactional
    public Long updatePerformance(Long performanceId, PerformanceCreateRequestDto dto, User user) {
        if (user == null || user.getUserId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        Performance performance = performanceRepository.findById(performanceId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "공연을 찾을 수 없습니다. performanceId=" + performanceId));

        PerformerProfile creator = performance.getPerformerCreator();
        if (creator == null || creator.getUser() == null || !creator.getUser().getUserId().equals(user.getUserId())) {
            throw new ApiException(ErrorCode.FORBIDDEN, "본인이 생성한 공연만 수정할 수 있습니다.");
        }

        if (StringUtils.hasText(dto.getTitle())) {
            performance.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            performance.setDescription(dto.getDescription());
        }
        if (dto.getPerformanceImageUrl() != null) {
            performance.setPerformanceImageUrl(dto.getPerformanceImageUrl());
        }
        if (dto.getCapacity() != null) {
            performance.setCapacity(dto.getCapacity());
        }
        PerformanceCategory categoryEnum = dto.toCategoryEnum();
        if (categoryEnum != null) {
            performance.setCategory(categoryEnum);
        }

        if (dto.getVenueId() != null && !dto.getVenueId().equals(
            performance.getVenue() != null ? performance.getVenue().getVenueId() : null)) {
            Venue venue = venueRepository.findByVenueIdAndIsDeletedFalse(dto.getVenueId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "공연장을 찾을 수 없습니다. venueId=" + dto.getVenueId()));
            performance.setVenue(venue);
            performance.setHostCreator(venue.getHost());
        }

        log.info("[Performance] updated - performanceId={}", performanceId);
        return performanceId;
    }

    /**
     * 공연을 논리 삭제합니다.
     * - 본인이 생성한 공연자만 삭제 가능.
     */
    @Transactional
    public void deletePerformance(Long performanceId, User user) {
        if (user == null || user.getUserId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        Performance performance = performanceRepository.findById(performanceId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "공연을 찾을 수 없습니다. performanceId=" + performanceId));

        PerformerProfile creator = performance.getPerformerCreator();
        if (creator == null || creator.getUser() == null || !creator.getUser().getUserId().equals(user.getUserId())) {
            throw new ApiException(ErrorCode.FORBIDDEN, "본인이 생성한 공연만 삭제할 수 있습니다.");
        }

        performance.delete();
        log.info("[Performance] deleted(soft) - performanceId={}", performanceId);
    }

    /**
     * 현재 로그인한 유저가 해당 공연을 수정/삭제할 수 있는지 여부를 반환합니다.
     */
    public boolean isEditableByUser(Long performanceId, User user) {
        if (user == null || user.getUserId() == null) return false;
        return performanceRepository.findById(performanceId)
            .map(p -> {
                PerformerProfile creator = p.getPerformerCreator();
                return creator != null
                    && creator.getUser() != null
                    && creator.getUser().getUserId().equals(user.getUserId());
            })
            .orElse(false);
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
            review.getEncorePick(),
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
    public Long createPerformanceReview(Long performanceId, Long userId, Integer rating, String content, String encorePick) {

        if (rating == null || rating < 1 || rating > 5) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "별점은 1~5점이어야 합니다.");
        }

        String c = (content == null) ? "" : content.trim();
        if (c.length() < 5) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "리뷰는 5자 이상 입력해 주세요.");
        }

        String ep = (encorePick == null || encorePick.isBlank()) ? null : encorePick.trim();
        if (ep != null && ep.length() > 200) {
            ep = ep.substring(0, 200);
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
            .encorePick(ep)
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
            "content", review.getContent(),
            "encorePick", review.getEncorePick() != null ? review.getEncorePick() : ""
        );
    }

    @Transactional
    public void updatePerformanceReview(
        Long performanceId,
        Long reviewId,
        Long userId,
        Integer rating,
        String content,
        String encorePick
    ) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "별점은 1~5점이어야 합니다.");
        }

        String c = (content == null) ? "" : content.trim();
        if (c.length() < 5) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "리뷰는 5자 이상 입력해 주세요.");
        }

        String ep = (encorePick == null || encorePick.isBlank()) ? null : encorePick.trim();
        if (ep != null && ep.length() > 200) {
            ep = ep.substring(0, 200);
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
        review.setEncorePick(ep);
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

    // ─── 좌석 리뷰 (관람객 전용, Controller에서 ROLE_USER 검증) ─────────────────────

    /**
     * [설명] 해당 공연이 열리는 공연장의 좌석 목록을 조회한다.
     * 좌석 리뷰 작성 시 좌석 선택 드롭다운용.
     *
     * @param performanceId 공연 ID
     * @return 좌석 옵션 DTO 목록
     * @throws ApiException 공연 또는 공연장이 없을 경우 NOT_FOUND
     */
    public List<SeatOptionDto> getSeatsByPerformanceId(Long performanceId) {
        // venue를 fetch join으로 함께 로드해 좌석 조회 시 NPE·Lazy 로딩 이슈 방지
        Performance performance = performanceRepository.findDetailById(performanceId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "공연을 찾을 수 없습니다. performanceId=" + performanceId));

        if (performance.getVenue() == null) {
            log.warn("[SeatReview] 공연에 장소가 없음 - performanceId={}", performanceId);
            return List.of();
        }

        List<Seat> seats = seatRepository.findAllByVenueAndIsDeletedFalse(performance.getVenue());
        return seats.stream().map(SeatOptionDto::new).toList();
    }

    /**
     * [설명] 특정 공연의 좌석 리뷰 목록을 페이징 조회한다.
     *
     * @param performanceId 공연 ID
     * @param pageable      페이징
     * @return 좌석 리뷰 페이지 (SeatReviewItemDto)
     */
    public Page<SeatReviewItemDto> getSeatReviews(Long performanceId, Pageable pageable) {
        if (!performanceRepository.existsById(performanceId)) {
            throw new ApiException(ErrorCode.NOT_FOUND, "공연을 찾을 수 없습니다. performanceId=" + performanceId);
        }

        Page<Review> page = reviewRepository
            .findByPerformance_PerformanceIdAndSeatIsNotNullAndIsDeletedFalseOrderByCreatedAtDesc(performanceId, pageable);

        return page.map(r -> toSeatReviewItemDto(r));
    }

    /**
     * 해당 공연의 좌석 리뷰를 좌석 배치도 호버 툴팁용으로 전부 조회 (최대 500건).
     * 프론트에서 seatId별로 그룹해 마우스 오버 시 리뷰 내용을 표시할 때 사용.
     *
     * @param performanceId 공연 ID
     * @return 좌석 리뷰 목록 (seatId, rating, content 등 포함)
     */
    public List<SeatReviewItemDto> getSeatReviewsForMap(Long performanceId) {
        if (!performanceRepository.existsById(performanceId)) {
            throw new ApiException(ErrorCode.NOT_FOUND, "공연을 찾을 수 없습니다. performanceId=" + performanceId);
        }
        Page<Review> page = reviewRepository
            .findByPerformance_PerformanceIdAndSeatIsNotNullAndIsDeletedFalseOrderByCreatedAtDesc(
                performanceId, PageRequest.of(0, 500));
        return page.getContent().stream().map(this::toSeatReviewItemDto).toList();
    }

    private SeatReviewItemDto toSeatReviewItemDto(Review r) {
        return new SeatReviewItemDto(
            r.getReviewId(),
            r.getUser() != null ? r.getUser().getUserId() : 0L,
            r.getUser() != null ? r.getUser().getNickname() : "-",
            r.getRating(),
            r.getContent(),
            r.getSeat() != null ? r.getSeat().getSeatId() : null,
            r.getSeat() != null ? r.getSeat().getSeatNumber() : null,
            r.getSeat() != null ? r.getSeat().getSeatType() : null,
            r.getSeat() != null ? r.getSeat().getSeatFloor() : null,
            r.getCreatedAt()
        );
    }

    /**
     * [설명] 해당 공연의 좌석 리뷰 평균 별점·개수 요약을 반환한다.
     *
     * @param performanceId 공연 ID
     * @return "avgRating", "reviewCount" 키의 Map
     */
    public Map<String, Object> getSeatReviewSummary(Long performanceId) {
        Object[] row = reviewRepository.getSeatReviewSummary(performanceId);
        double avgRating = 0.0;
        long reviewCount = 0L;
        if (row != null && row.length >= 2) {
            avgRating = row[0] != null ? ((Number) row[0]).doubleValue() : 0.0;
            reviewCount = row[1] != null ? ((Number) row[1]).longValue() : 0L;
        }
        return Map.of("avgRating", avgRating, "reviewCount", reviewCount);
    }

    /**
     * [설명] 좌석 리뷰를 등록한다. (관람객 전용 — Controller에서 ROLE_USER 검증)
     * 좌석은 해당 공연의 공연장(venue) 소속이어야 한다.
     *
     * @param performanceId 공연 ID
     * @param userId         작성자 회원 ID (관람객)
     * @param seatId         좌석 ID
     * @param rating         별점 1~5
     * @param content        리뷰 내용 5자 이상
     * @return 생성된 리뷰 ID
     */
    @Transactional
    public Long createSeatReview(Long performanceId, Long userId, Long seatId, Integer rating, String content) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "별점은 1~5점이어야 합니다.");
        }
        String c = (content == null) ? "" : content.trim();
        if (c.length() < 5) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "리뷰는 5자 이상 입력해 주세요.");
        }
        if (seatId == null) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "좌석을 선택해 주세요.");
        }

        Performance performance = performanceRepository.findById(performanceId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "공연을 찾을 수 없습니다. performanceId=" + performanceId));

        Seat seat = seatRepository.findById(seatId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "좌석을 찾을 수 없습니다. seatId=" + seatId));

        if (performance.getVenue() == null || !performance.getVenue().getVenueId().equals(seat.getVenue().getVenueId())) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "해당 공연 장소의 좌석만 선택할 수 있습니다.");
        }

        Review review = Review.builder()
            .performance(performance)
            .user(User.builder().userId(userId).build())
            .seat(seat)
            .rating(rating)
            .content(c)
            .build();

        Long savedId = reviewRepository.save(review).getReviewId();
        log.info("[SeatReview] 작성 완료 - performanceId={}, seatId={}, reviewId={}", performanceId, seatId, savedId);
        return savedId;
    }

    /**
     * [설명] 좌석 리뷰 수정 폼용 단건 조회. 작성자 본인만 조회 가능.
     *
     * @param performanceId 공연 ID
     * @param reviewId      리뷰 ID
     * @param userId        요청자 회원 ID
     * @return rating, content, seatId 포함 Map
     */
    public Map<String, Object> getSeatReviewForEdit(Long performanceId, Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "리뷰를 찾을 수 없습니다."));

        if (review.getPerformance() == null || !performanceId.equals(review.getPerformance().getPerformanceId())) {
            throw new ApiException(ErrorCode.NOT_FOUND, "리뷰를 찾을 수 없습니다.");
        }
        if (review.getSeat() == null) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "공연 리뷰는 이 API 대상이 아닙니다.");
        }
        if (review.getUser() == null || !userId.equals(review.getUser().getUserId())) {
            throw new ApiException(ErrorCode.FORBIDDEN, "수정 권한이 없습니다.");
        }

        return Map.of(
            "rating", review.getRating(),
            "content", review.getContent(),
            "seatId", review.getSeat().getSeatId()
        );
    }

    /**
     * [설명] 좌석 리뷰를 수정한다. 작성자 본인만 가능.
     *
     * @param performanceId 공연 ID
     * @param reviewId       리뷰 ID
     * @param userId         요청자 회원 ID
     * @param rating         별점 1~5
     * @param content        리뷰 내용 5자 이상
     */
    @Transactional
    public void updateSeatReview(Long performanceId, Long reviewId, Long userId, Integer rating, String content) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "별점은 1~5점이어야 합니다.");
        }
        String c = (content == null) ? "" : content.trim();
        if (c.length() < 5) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "리뷰는 5자 이상 입력해 주세요.");
        }

        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "리뷰를 찾을 수 없습니다. reviewId=" + reviewId));

        if (review.getPerformance() == null || !review.getPerformance().getPerformanceId().equals(performanceId)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "해당 공연의 리뷰가 아닙니다.");
        }
        if (review.getSeat() == null) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "공연 리뷰는 이 API 대상이 아닙니다.");
        }
        if (review.getUser() == null || !review.getUser().getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "수정 권한이 없습니다.");
        }

        review.setRating(rating);
        review.setContent(c);
        log.info("[SeatReview] 수정 완료 - reviewId={}", reviewId);
    }

    /**
     * [설명] 좌석 리뷰를 논리 삭제한다. 작성자 본인만 가능.
     *
     * @param performanceId 공연 ID
     * @param reviewId      리뷰 ID
     * @param userId        요청자 회원 ID
     */
    @Transactional
    public void deleteSeatReview(Long performanceId, Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "리뷰를 찾을 수 없습니다. reviewId=" + reviewId));

        if (review.getPerformance() == null || !review.getPerformance().getPerformanceId().equals(performanceId)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "공연에 속한 리뷰가 아닙니다.");
        }
        if (review.getUser() == null || !review.getUser().getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "삭제 권한이 없습니다.");
        }

        review.delete();
        log.info("[SeatReview] 삭제 완료 - reviewId={}", reviewId);
    }
}
