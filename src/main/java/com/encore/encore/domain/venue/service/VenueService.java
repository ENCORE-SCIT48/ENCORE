package com.encore.encore.domain.venue.service;

import com.encore.encore.domain.member.entity.HostProfile;
import com.encore.encore.domain.member.repository.HostProfileRepository;
import com.encore.encore.domain.user.entity.User;
import com.encore.encore.domain.venue.dto.*;
import com.encore.encore.domain.venue.entity.Seat;
import com.encore.encore.domain.venue.entity.Venue;
import com.encore.encore.domain.venue.repository.SeatRepository;
import com.encore.encore.domain.venue.repository.VenueRepository;
import com.encore.encore.global.common.service.FileService;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VenueService {
    private final SeatRepository seatRepository;
    private final VenueRepository venueRepository;
    private final HostProfileRepository hostProfileRepository;
    private final FileService fileService;

    /**
     * 공연장 목록을 조회. 검색어가 있으면 이름/주소 기준 검색, 없으면 전체 조회.
     *
     * @param keyword  검색어
     * @param pageable 페이징 정보
     * @return 공연장 목록(페이지)
     */
    public Page<VenueListItemDto> getVenues(String keyword, Pageable pageable) {

        // 컨트롤러가 아니라 서비스에서 "검색/전체조회 분기"를 처리해야
        // 다른 API에서도 재사용 가능하고, 컨트롤러가 얇아져 유지보수성이 좋아짐
        Page<Venue> venues = StringUtils.hasText(keyword)
            ? venueRepository.findByVenueNameContainingIgnoreCaseAndIsDeletedFalseOrAddressContainingIgnoreCaseAndIsDeletedFalse(keyword, keyword, pageable)
            : venueRepository.findByIsDeletedFalse(pageable);

        log.info("[Venue] list result - keyword={}, totalElements={}", keyword, venues.getTotalElements());
        return venues.map(VenueListItemDto::new);
    }

    /**
     * 공연장 상세 정보를 조회. 대상이 없으면 NOT_FOUND 예외를 발생.
     *
     * @param venueId 공연장 ID
     * @return 공연장 상세 DTO
     */
    public VenueDetailDto getVenue(Long venueId) {
        Venue venue = venueRepository.findByVenueIdAndIsDeletedFalse(venueId)
            // detailMessage까지 주면 사용자 메시지/로그 모두 더 명확해짐
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "공연장을 찾을 수 없습니다. venueId=" + venueId));

        log.info("[Venue] detail found - venueId={}", venueId);
        return new VenueDetailDto(venue);
    }

    /**
     * [설명] 신규 공연장과 해당 공연장의 좌석 정보를 일괄 등록합니다.
     *
     * @param dto       공연장 및 좌석 정보 요청 객체
     * @param imageFile 공연장 대표 이미지 파일
     * @param user      등록을 수행하는 호스트 유저 엔티티
     * @return 생성된 공연장의 ID
     */
    @Transactional
    public Long createVenue(VenueCreateRequestDto dto, MultipartFile imageFile, User user) {
        // 호스트 프로필 존재 여부 확인
        HostProfile host = hostProfileRepository.findByUser(user)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "프로필을 찾을 수 없습니다."));

        log.info("2026-02-25, [공연장 등록 프로세스 시작], HostID: {}, VenueName: {}", host.getHostId(), dto.getVenueName());

        // 이미지 저장
        String savedImagePath = (imageFile != null && !imageFile.isEmpty())
            ? fileService.saveFile(imageFile) : null;
        // 좌석 엔티티 먼저 만들기 (임시로 venue 없이)
        int totalSeats = dto.getSeats() != null ? dto.getSeats().size() : 0;

        // Venue 엔티티 생성
        Venue venue = Venue.builder()
            .host(host)
            .venueName(dto.getVenueName())
            .address(dto.getAddress())
            .contact(dto.getContact())
            .description(dto.getDescription())
            .venueType(dto.getVenueType())
            .venueImage(savedImagePath)
            .totalSeats(dto.getTotalSeats())
            .openTime(dto.getOpenTime())
            .closeTime(dto.getCloseTime())
            .bookingUnit(dto.getBookingUnit())
            .rentalFee(dto.getRentalFee())
            .regularClosing(dto.getRegularClosingDaysAsString()) // DTO 메서드 호출
            .totalSeats(totalSeats)
            .temporaryClosing(dto.getTemporaryClosingDatesAsString())
            .facilities(dto.getFacilitiesAsString())
            .build();

        Venue savedVenue = venueRepository.save(venue);
        log.info("2026-02-25, [공연장 기본 정보 저장 완료], VenueID: {}", savedVenue.getVenueId());

        // 좌석 정보(Seat) 리스트 일괄 변환 및 저장
        List<Seat> seatEntities = dto.toSeatEntities(savedVenue);
        if (!seatEntities.isEmpty()) {
            seatRepository.saveAll(seatEntities);
            log.info("2026-02-25, [좌석 일괄 저장 완료], Count: {}", seatEntities.size());
        }

        return savedVenue.getVenueId();
    }

    /**
     * [설명] 기존 공연장 정보 및 좌석 레이아웃을 수정합니다.
     * 새 이미지가 업로드될 경우 기존 파일을 삭제하여 서버 자원을 관리합니다.
     * [작업자] 2026-02-25, feature/venue-update-#33
     *
     * @param venueId   수정 대상 공연장 PK
     * @param dto       수정할 데이터 객체
     * @param imageFile 신규 이미지 파일 (선택 사항)
     * @param user      수정 요청 유저 (권한 확인용)
     * @return 수정 완료된 공연장 ID
     */
    @Transactional
    public Long updateVenue(Long venueId, VenueCreateRequestDto dto, MultipartFile imageFile, User user) {
        // 1. 기존 데이터 조회 및 검증
        Venue venue = venueRepository.findById(venueId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "수정할 공연장을 찾을 수 없습니다."));

        // 2. 권한 검증: 본인의 공연장인지 확인 (보안 강화)
        if (!venue.getHost().getUser().getUserId().equals(user.getUserId())) {
            log.error("2026-02-25, [공연장 수정 권한 없음], VenueID: {}, UserID: {}", venueId, user.getUserId());
            throw new ApiException(ErrorCode.FORBIDDEN, "본인이 등록한 공연장만 수정할 수 있습니다.");
        }

        log.info("2026-02-25, [공연장 수정 프로세스 시작], VenueID: {}, User: {}", venueId, user.getUserId());

        // 3. 이미지 업데이트 처리 (기존 파일 자원 관리 포함)
        String finalImagePath = venue.getVenueImage(); // 기본은 기존 경로 유지
        if (imageFile != null && !imageFile.isEmpty()) {
            // 기존 물리 파일 삭제
            if (venue.getVenueImage() != null) {
                fileService.deletePhysicalFile(venue.getVenueImage());
            }
            // 새 파일 저장
            finalImagePath = fileService.saveFile(imageFile);
            log.info("2026-02-25, [공연장 이미지 교체 완료], NewPath: {}", finalImagePath);
        }
        // 좌석 수 먼저 계산
        int totalSeats = dto.getSeats() != null ? dto.getSeats().size() : 0;

        // 엔티티 정보 갱신
        venue.updateVenueInfo(
            dto.getVenueName(), dto.getAddress(), dto.getContact(), dto.getDescription(),
            dto.getVenueType(), finalImagePath, dto.getOpenTime(), dto.getCloseTime(),
            dto.getBookingUnit(), dto.getRentalFee(), dto.getFacilitiesAsString(),
            totalSeats, dto.getRegularClosingDaysAsString(), dto.getTemporaryClosingDatesAsString()
        );

        // 3. 좌석 정보 갱신
        seatRepository.deleteByVenue(venue);
        List<Seat> newSeats = dto.toSeatEntities(venue);
        if (!newSeats.isEmpty()) {
            seatRepository.saveAll(newSeats);
        }

        log.info("2026-02-25, [공연장 수정 프로세스 종료], VenueID: {}", venueId);
        return venue.getVenueId();
    }

    /**
     * [설명] 공연장 및 소속 좌석 정보를 논리 삭제(Soft Delete)합니다.
     *
     * @param venueId 삭제 대상 공연장 id
     * @param user    로그인중인 유저
     */
    @Transactional
    public void deleteVenue(Long venueId, User user) {
        // 1. 공연장 존재 확인
        Venue venue = venueRepository.findById(venueId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "삭제할 공연장을 찾을 수 없습니다."));

        // 2. 권한 확인
        if (!venue.getHost().getUser().getUserId().equals(user.getUserId())) {
            log.error("2026-02-25, [삭제 권한 없음], VenueID: {}, UserID: {}", venueId, user.getUserId());
            throw new ApiException(ErrorCode.FORBIDDEN, "본인이 등록한 공연장만 삭제할 수 있습니다.");
        }

        // 3. 공연장 논리 삭제
        venue.delete(); // isDeleted = true
        log.info("2026-02-25, [공연장 논리 삭제 완료], VenueID: {}", venueId);

        // 2. 활성 상태인 좌석들만 논리 삭제
        List<Seat> seats = seatRepository.findAllByVenueAndIsDeletedFalse(venue);
        if (!seats.isEmpty()) {
            seats.forEach(Seat::delete);
        }

        log.info("2026-02-25, [공연장 논리 삭제 완료], VenueID: {}", venueId);
    }

    /**
     * [설명] 공연장 등록/수정 폼 데이터를 조회합니다. (좌석 정보 포함)
     *
     * @param venueId 공연장 ID
     * @return 폼 prefill용 DTO (floorLayouts 포함)
     */
    public VenueFormResponseDto getVenueForm(Long venueId) {
        Venue venue = venueRepository.findByVenueIdAndIsDeletedFalse(venueId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND,
                "공연장을 찾을 수 없습니다. venueId=" + venueId));

        List<Seat> seats = seatRepository.findAllByVenueAndIsDeletedFalse(venue);

        log.info("2026-02-26, [공연장 폼 데이터 조회], venueId={}, seatCount={}", venueId, seats.size());
        return new VenueFormResponseDto(venue, seats);
    }

    /**
     * [호스트] 내 공연장 목록 조회.
     * GET /api/venues/my 전용. 삭제되지 않은 공연장만 반환한다.
     *
     * @param hostId 호스트 프로필 ID (CustomUserDetails.activeProfileId)
     * @return 호스트 소유 공연장 목록 (VenueMyListItemDto)
     */
    public List<VenueMyListItemDto> getMyVenues(Long hostId) {
        List<VenueMyListItemDto> result = venueRepository.findByHost_HostIdAndIsDeletedFalse(hostId)
            .stream()
            .map(VenueMyListItemDto::new)
            .toList();
        log.info("[Venue] my venues - hostId={}, count={}", hostId, result.size());
        return result;
    }
}
