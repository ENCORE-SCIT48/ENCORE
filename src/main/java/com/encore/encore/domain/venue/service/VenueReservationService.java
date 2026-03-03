package com.encore.encore.domain.venue.service;

import com.encore.encore.domain.member.entity.HostProfile;
import com.encore.encore.domain.member.entity.PerformerProfile;
import com.encore.encore.domain.member.repository.PerformerProfileRepository;
import com.encore.encore.domain.venue.dto.ReservationRejectRequestDto;
import com.encore.encore.domain.venue.dto.VenueReservationRequestDto;
import com.encore.encore.domain.venue.dto.VenueReservationResponseDto;
import com.encore.encore.domain.venue.entity.ReservationStatus;
import com.encore.encore.domain.venue.entity.Venue;
import com.encore.encore.domain.venue.entity.VenueReservation;
import com.encore.encore.domain.venue.repository.VenueRepository;
import com.encore.encore.domain.venue.repository.VenueReservationRepository;
import com.encore.encore.global.config.CustomUserDetails;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 대관 예약 비즈니스 로직 서비스.
 * 공연자의 신청, 호스트의 승낙/거절, 목록 조회를 담당한다.
 * 인증 정보는 CustomUserDetails 의 activeProfileId 를 사용한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VenueReservationService {

    private final VenueReservationRepository reservationRepository;
    private final VenueRepository venueRepository;
    private final PerformerProfileRepository performerProfileRepository;

    /**
     * 공연자가 공연장에 대관을 신청한다.
     * 공연장의 host 는 Venue 엔티티에서 직접 참조한다.
     *
     * @param userDetails 로그인 사용자 정보 (activeProfileId = performerId)
     * @param dto         대관 신청 요청 정보
     * @return 생성된 예약 응답 DTO
     * @throws ApiException 공연장 또는 공연자를 찾을 수 없을 경우 (NOT_FOUND)
     * @throws ApiException 종료 일시가 시작 일시보다 이전일 경우 (INVALID_REQUEST)
     */
    @Transactional
    public VenueReservationResponseDto createReservation(CustomUserDetails userDetails, VenueReservationRequestDto dto) {
        Long performerId = userDetails.getActiveProfileId();
        log.info("[대관 신청] performerId={}, venueId={}", performerId, dto.getVenueId());

        if (!dto.getEndAt().isAfter(dto.getStartAt())) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "종료 일시는 시작 일시 이후여야 합니다.");
        }

        Venue venue = findVenueById(dto.getVenueId());

        // 운영 시간 및 휴무일, 예약 단위 검증
        validateReservationTimeAndBusinessRules(venue, dto.getStartAt(), dto.getEndAt());

        // 일정 중복(충돌) 체크 - PENDING/APPROVED 상태만 고려
        boolean hasConflict = reservationRepository.existsByVenueAndStatusInAndEndAtGreaterThanAndStartAtLessThan(
            venue,
            List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED),
            dto.getStartAt(),
            dto.getEndAt()
        );
        if (hasConflict) {
            throw new ApiException(ErrorCode.CONFLICT, "이미 해당 시간에 대관 예약이 존재합니다.");
        }

        HostProfile host = venue.getHost(); // Venue → HostProfile 직접 참조

        PerformerProfile performer = performerProfileRepository.findById(performerId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "공연자 프로필을 찾을 수 없습니다. performerId=" + performerId));

        VenueReservation reservation = VenueReservation.builder()
            .venue(venue)
            .host(host)
            .performer(performer)
            .startAt(dto.getStartAt())
            .endAt(dto.getEndAt())
            .message(dto.getMessage())
            .build();

        VenueReservation saved = reservationRepository.save(reservation);
        log.info("[대관 신청 완료] reservationId={}", saved.getReservationId());

        return VenueReservationResponseDto.from(saved);
    }

    /**
     * 공연자의 대관 요청 목록을 조회한다.
     *
     * @param userDetails 로그인 사용자 정보 (activeProfileId = performerId)
     * @return 해당 공연자의 전체 예약 응답 DTO 목록
     */
    public List<VenueReservationResponseDto> getMyReservations(CustomUserDetails userDetails) {
        Long performerId = userDetails.getActiveProfileId();
        log.info("[내 대관 목록 조회] performerId={}", performerId);

        return reservationRepository.findAllByPerformerIdWithDetails(performerId)
            .stream()
            .map(VenueReservationResponseDto::from)
            .toList();
    }

    /**
     * 호스트가 소유한 특정 공연장의 대관 요청 목록을 조회한다.
     *
     * @param userDetails 로그인 사용자 정보 (activeProfileId = hostId)
     * @param venueId     조회할 공연장 ID
     * @return 해당 공연장의 전체 예약 응답 DTO 목록
     * @throws ApiException 공연장을 찾을 수 없을 경우 (NOT_FOUND)
     * @throws ApiException 해당 호스트가 공연장 소유자가 아닐 경우 (FORBIDDEN)
     */
    public List<VenueReservationResponseDto> getReservationsByVenue(CustomUserDetails userDetails, Long venueId) {
        Long hostId = userDetails.getActiveProfileId();
        log.info("[공연장 대관 목록 조회] hostId={}, venueId={}", hostId, venueId);

        Venue venue = findVenueById(venueId);
        validateHostOwnership(hostId, venue);

        return reservationRepository.findAllByVenueIdWithDetails(venueId)
            .stream()
            .map(VenueReservationResponseDto::from)
            .toList();
    }

    /**
     * 호스트가 대관 요청을 승낙한다.
     *
     * @param userDetails   로그인 사용자 정보 (activeProfileId = hostId)
     * @param reservationId 승낙할 예약 ID
     * @return 승낙 처리된 예약 응답 DTO
     * @throws ApiException 예약을 찾을 수 없을 경우 (NOT_FOUND)
     * @throws ApiException 해당 호스트가 공연장 소유자가 아닐 경우 (FORBIDDEN)
     * @throws ApiException PENDING 상태가 아닐 경우 (INVALID_REQUEST)
     */
    @Transactional
    public VenueReservationResponseDto approveReservation(CustomUserDetails userDetails, Long reservationId) {
        Long hostId = userDetails.getActiveProfileId();
        log.info("[대관 승낙] hostId={}, reservationId={}", hostId, reservationId);

        VenueReservation reservation = findReservationById(reservationId);
        validateHostOwnership(hostId, reservation.getVenue());

        reservation.approve();
        VenueReservation saved = reservationRepository.save(reservation); // 명시적 save
        return VenueReservationResponseDto.from(saved);
    }

    /**
     * 호스트가 대관 요청을 거절한다.
     *
     * @param userDetails   로그인 사용자 정보 (activeProfileId = hostId)
     * @param reservationId 거절할 예약 ID
     * @param dto           거절 사유 요청 DTO
     * @return 거절 처리된 예약 응답 DTO
     * @throws ApiException 예약을 찾을 수 없을 경우 (NOT_FOUND)
     * @throws ApiException 해당 호스트가 공연장 소유자가 아닐 경우 (FORBIDDEN)
     * @throws ApiException PENDING 상태가 아닐 경우 (INVALID_REQUEST)
     */
    @Transactional
    public VenueReservationResponseDto rejectReservation(CustomUserDetails userDetails, Long reservationId,
                                                         ReservationRejectRequestDto dto) {
        Long hostId = userDetails.getActiveProfileId();
        log.info("[대관 거절] hostId={}, reservationId={}", hostId, reservationId);

        VenueReservation reservation = findReservationById(reservationId);
        validateHostOwnership(hostId, reservation.getVenue());

        reservation.reject(dto.getRejectReason());
        log.info("[대관 거절 완료] reservationId={}", reservationId);

        return VenueReservationResponseDto.from(reservation);
    }

    /**
     * 공연장 ID로 Venue 를 조회한다.
     *
     * @param venueId 공연장 ID
     * @return Venue 엔티티
     * @throws ApiException 공연장을 찾을 수 없을 경우 (NOT_FOUND)
     */
    private Venue findVenueById(Long venueId) {
        return venueRepository.findById(venueId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND,
                "공연장을 찾을 수 없습니다. venueId=" + venueId));
    }

    /**
     * 예약 ID로 VenueReservation 을 조회한다.
     *
     * @param reservationId 예약 ID
     * @return VenueReservation 엔티티
     * @throws ApiException 예약을 찾을 수 없을 경우 (NOT_FOUND)
     */
    private VenueReservation findReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND,
                "예약을 찾을 수 없습니다. reservationId=" + reservationId));
    }

    /**
     * 호스트가 해당 공연장의 소유자인지 검증한다.
     * Venue.host.hostId 와 activeProfileId 를 비교한다.
     *
     * @param hostId 검증할 호스트 프로필 ID
     * @param venue  검증 대상 공연장
     * @throws ApiException 소유자가 아닐 경우 (FORBIDDEN)
     */
    private void validateHostOwnership(Long hostId, Venue venue) {
        if (!venue.getHost().getHostId().equals(hostId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "해당 공연장에 대한 권한이 없습니다. hostId=" + hostId);
        }
    }

    /**
     * 공연장 운영 시간, 휴무일, 예약 단위 관점에서 대관 가능 여부를 검증한다.
     *
     * @param venue   공연장
     * @param startAt 시작 일시
     * @param endAt   종료 일시
     */
    private void validateReservationTimeAndBusinessRules(Venue venue, LocalDateTime startAt, LocalDateTime endAt) {
        LocalDate date = startAt.toLocalDate();
        if (!date.equals(endAt.toLocalDate())) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "대관은 하루 단위로만 신청할 수 있습니다.");
        }

        // 정기 휴무일 검증
        String regularClosing = venue.getRegularClosing();
        if (regularClosing != null && !regularClosing.isBlank()) {
            Set<String> closingDays = Arrays.stream(regularClosing.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

            DayOfWeek dayOfWeek = date.getDayOfWeek(); // MONDAY ...
            if (closingDays.contains(dayOfWeek.name())) {
                throw new ApiException(ErrorCode.INVALID_REQUEST, "정기 휴무일에는 대관을 신청할 수 없습니다.");
            }
        }

        // 임시 휴무일 검증 (yyyy-MM-dd 문자열 비교)
        String temporaryClosing = venue.getTemporaryClosing();
        if (temporaryClosing != null && !temporaryClosing.isBlank()) {
            Set<String> tempDates = Arrays.stream(temporaryClosing.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

            String target = date.toString(); // ISO-8601, 예: 2026-03-01
            if (tempDates.contains(target)) {
                throw new ApiException(ErrorCode.INVALID_REQUEST, "임시 휴무일에는 대관을 신청할 수 없습니다.");
            }
        }

        // 운영 시간 범위 및 예약 단위 검증
        String open = venue.getOpenTime();
        String close = venue.getCloseTime();
        Integer unit = venue.getBookingUnit();

        if (open != null && close != null) {
            LocalTime openTime = parseTime(open);
            LocalTime closeTime = parseTime(close);

            LocalTime startTime = startAt.toLocalTime();
            LocalTime endTime = endAt.toLocalTime();

            if (startTime.isBefore(openTime) || endTime.isAfter(closeTime)) {
                throw new ApiException(ErrorCode.INVALID_REQUEST, "공연장 운영 시간 범위 내에서만 대관을 신청할 수 있습니다.");
            }

            if (unit != null && unit > 0) {
                long minutes = Duration.between(startTime, endTime).toMinutes();
                if (minutes <= 0 || minutes % unit != 0) {
                    throw new ApiException(ErrorCode.INVALID_REQUEST, "대관 시간은 예약 단위(" + unit + "분) 에 맞게 선택해야 합니다.");
                }
            }
        }
    }

    private LocalTime parseTime(String hhmm) {
        String[] parts = hhmm.split(":");
        if (parts.length != 2) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "잘못된 시간 형식입니다: " + hhmm);
        }
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        return LocalTime.of(h, m);
    }
}
