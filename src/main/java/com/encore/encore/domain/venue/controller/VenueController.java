package com.encore.encore.domain.venue.controller;

import com.encore.encore.domain.venue.dto.*;
import com.encore.encore.domain.venue.service.VenueService;
import com.encore.encore.global.common.CommonResponse;
import com.encore.encore.global.config.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    /**
     * 공연장 목록을 조회 (검색/페이징 지원)
     *
     * @param keyword 검색어(공연장명 또는 주소) - null/빈값이면 전체 조회
     * @param page    페이지 번호(0부터 시작)
     * @param size    페이지 당 개수
     * @return 공연장 목록 페이지
     */
    @GetMapping
    public CommonResponse<Page<VenueListItemDto>> getVenues(
        @RequestParam(name = "search", required = false) String keyword,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        log.info("[Venue] list request - keyword={}, page={}, size={}", keyword, page, size); // [추가] INFO 로그

        return CommonResponse.ok(
            venueService.getVenues(keyword, PageRequest.of(page, size)),
            "공연장 목록 조회 성공"
        );
    }

    /**
     * 공연장 상세 정보를 조회
     *
     * @param venueId 공연장 ID
     * @return 공연장 상세 정보
     */
    @GetMapping("/{venueId}")
    public CommonResponse<VenueDetailDto> getVenue(@PathVariable("venueId") Long venueId) {
        log.info("[Venue] detail request - venueId={}", venueId); // [추가] INFO 로그

        return CommonResponse.ok(
            venueService.getVenue(venueId),
            "공연장 상세 조회 성공"
        );
    }

    /**
     * [설명] 신규 공연장을 등록합니다. (이미지 파일 포함)
     *
     * @param dto         데이터 (RequestPart - JSON)
     * @param imageFile   신규 이미지 파일 (RequestPart - File, Optional)
     * @param userDetails 인증된 호스트 정보
     * @return 등록된 공연장 ID
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResponse<Long> createVenue(
        @RequestPart("venueData") VenueCreateRequestDto dto,
        @RequestPart(value = "venueImage", required = false) MultipartFile imageFile,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("2026-02-25, [공연장 등록 호출], User: {}, ImagePresent: {}",
            userDetails.getUsername(), (imageFile != null));

        Long venueId = venueService.createVenue(dto, imageFile, userDetails.getUser());

        return CommonResponse.ok(venueId, "공연장이 성공적으로 등록되었습니다.");
    }

    /**
     * [설명] 기존 공연장 정보를 수정합니다. (이미지 포함 시 Multipart/form-data)
     *
     * @param venueId     수정할 공연장 ID (Path Variable)
     * @param dto         수정 데이터 (RequestPart - JSON)
     * @param imageFile   신규 이미지 파일 (RequestPart - File, Optional)
     * @param userDetails 인증된 호스트 정보
     * @return 수정 완료된 공연장 ID
     */
    @PutMapping(value = "/{venueId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public CommonResponse<Long> updateVenue(
        @PathVariable("venueId") Long venueId,
        @RequestPart("venueData") VenueCreateRequestDto dto,
        @RequestPart(value = "venueImage", required = false) MultipartFile imageFile,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 1. 요청 로그 기록 (작업자, 일시, 주요 파라미터 포함)
        log.info("2026-02-25, [공연장 수정 API 호출], VenueID: {}, User: {}, HasNewImage: {}",
            venueId, userDetails.getUsername(), (imageFile != null && !imageFile.isEmpty()));

        // 2. 서비스 로직 호출 (본인 확인 권한 로직은 서비스 내부에 포함됨)
        Long updatedId = venueService.updateVenue(venueId, dto, imageFile, userDetails.getUser());

        // 3. 완료 로그 및 표준 응답 반환
        log.info("2026-02-25, [공연장 수정 API 성공], Updated VenueID: {}", updatedId);

        return CommonResponse.ok(updatedId, "공연장 정보가 성공적으로 수정되었습니다.");
    }

    /**
     * [설명] 공연장 정보를 논리 삭제(Soft Delete)합니다.
     *
     * @param venueId     삭제 대상 공연장 ID
     * @param userDetails 현재 로그인한 호스트 정보
     * @return 204 No Content
     */
    @DeleteMapping("/{venueId}")
    public ResponseEntity<Void> deleteVenue(
        @PathVariable("venueId") Long venueId,
        @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("2026-02-25, [공연장 삭제 요청], VenueID: {}, User: {}", venueId, userDetails.getUser().getUserId());

        venueService.deleteVenue(venueId, userDetails.getUser());

        // 삭제 성공 시 별도의 데이터 반환 없이 204(No Content)를 응답하는 것이 Restful 가이드라인에 부합함
        return ResponseEntity.noContent().build();
    }

    /**
     * [설명] 공연장 등록/수정 폼 prefill용 데이터를 조회합니다.
     *
     * @param venueId 조회할 공연장 ID
     * @return 공연장 폼 데이터 (좌석 정보 포함)
     */
    @GetMapping("/{venueId}/form")
    public CommonResponse<VenueFormResponseDto> getVenueForm(@PathVariable("venueId") Long venueId) {
        log.info("2026-02-26, [공연장 폼 데이터 요청], venueId={}", venueId);
        return CommonResponse.ok(
            venueService.getVenueForm(venueId),
            "공연장 폼 데이터 조회 성공"
        );
    }

    /**
     * [호스트] 내 공연장 목록 조회.
     * venueReservations.js 탭 렌더링 및 list.js 내 공연장 식별(버튼 분기)에 사용한다.
     *
     * @param userDetails 인증된 호스트 정보 (activeProfileId = hostId)
     * @return 호스트 소유 공연장 목록
     */
    @GetMapping("/my")
    public CommonResponse<List<VenueMyListItemDto>> getMyVenues(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long hostId = userDetails.getActiveProfileId();
        log.info("[GET /api/venues/my] hostId={}", hostId);
        return CommonResponse.ok(venueService.getMyVenues(hostId), "내 공연장 목록 조회 성공");
    }
}
