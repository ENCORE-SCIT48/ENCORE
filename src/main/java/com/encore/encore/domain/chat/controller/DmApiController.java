package com.encore.encore.domain.chat.controller;

import com.encore.encore.domain.chat.dto.ResponseChatMessage;
import com.encore.encore.domain.chat.dto.dm.*;
import com.encore.encore.domain.chat.service.DmService;
import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.global.common.CommonResponse;
import com.encore.encore.global.config.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Transactional
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/dms")
public class DmApiController {

    private final DmService dmService;

    /**
     * 로그인한 사용자의 Pending 상태 DM 목록을 조회합니다.
     *
     * <p>사용자의 인증 정보를 기반으로, 현재 참여 중이며 상대방이 보낸 요청 대기 상태인 DM
     * 채팅방들을 가져옵니다. 반환되는 리스트에는 각 DM의 상대방 정보, 최근 메시지,
     * 참여 상태 등이 포함됩니다.
     *
     * @param userDetails 인증된 사용자 정보가 담긴 CustomUserDetails 객체
     * @return 요청 받은 DM 목록을 감싼 {@link CommonResponse} 객체와 HTTP 200 상태 코드
     */
    @GetMapping("/pending")
    public ResponseEntity<CommonResponse<List<ResponseListDmDto>>> pending(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        //Long activeProfileId = userDetails.getActiveProfileId();
        //ActiveMode activeMode = userDetails.getActiveMode();

        Long activeProfileId = 2L;
        ActiveMode activeMode = ActiveMode.ROLE_HOST;

        List<ResponseListDmDto> result = dmService.getPendingList(activeProfileId, activeMode);

        return ResponseEntity.ok(CommonResponse.ok(result, "pending 받은 DM을 불러왔습니다."));
    }

    /**
     * 참여 중인 DM 목록을 조회합니다.
     *
     * <p>
     * 현재 로그인한 사용자의 프로필 ID와 활성 모드를 기준으로, 사용자가 이미 참여 중인 DM 방의 목록을 반환합니다.
     * 테스트용으로는 하드코딩된 프로필 ID와 모드를 사용하고 있습니다.
     * </p>
     *
     * @return ResponseEntity<CommonResponse<List<ResponseListDmDto>>>
     * - 요청 성공 시, 참여 중인 DM 목록과 메시지를 포함한 공통 응답 객체를 반환
     */

    @GetMapping("/accepted")
    public ResponseEntity<CommonResponse<List<ResponseListDmDto>>> accepted(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        //Long activeProfileId = userDetails.getActiveProfileId();
        //ActiveMode activeMode = userDetails.getActiveMode();

        Long activeProfileId = 2L;
        ActiveMode activeMode = ActiveMode.ROLE_HOST;

        List<ResponseListDmDto> result = dmService.getAcceptedList(activeProfileId, activeMode);

        return ResponseEntity.ok(CommonResponse.ok(result, "accepted DM을 불러왔습니다."));
    }

    /**
     * 개인페이지에서 DM 요청 처리
     * - 기존 DM 확인 후 roomId 반환
     * - 없으면 새로 생성
     */
    @PostMapping
    public ResponseEntity<CommonResponse<ResponseDmRoomStatusDto>> createDm(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody RequestDmDto dto
    ) {
        //Long activeProfileId = userDetails.getActiveProfileId();
        //ActiveMode activeMode = userDetails.getActiveMode();

        Long activeProfileId = 2L;
        ActiveMode activeMode = ActiveMode.ROLE_HOST;

        ResponseDmRoomStatusDto result = dmService.requestDm(activeProfileId, activeMode, dto);

        return ResponseEntity.ok(CommonResponse.ok(result, "DM 요청 처리 완료"));
    }

    /**
     * 지정된 DM 채팅방에 메시지를 전송합니다.
     *
     * <p>사용자의 활성 프로필과 모드를 확인하고, 참여 상태를 검증한 후
     * 메시지를 전송합니다. 전송 성공 시 전송된 메시지 정보를 반환합니다.</p>
     *
     * @param request     전송할 메시지 정보가 담긴 {@link RequestSendDmDto} 객체
     * @param userDetails 현재 인증된 사용자 정보 {@link CustomUserDetails}
     * @return 전송된 메시지 정보와 상태 메시지를 포함한 {@link ResponseEntity} 객체
     * - {@link CommonResponse}로 래핑되어 전송됨
     */
    @PostMapping("/{roomId}/messages")
    public ResponseEntity<CommonResponse<ResponseSendDmDto>> sendMessage(
        @RequestBody RequestSendDmDto request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        //Long activeProfileId = userDetails.getActiveProfileId();
        //ActiveMode activeMode = userDetails.getActiveMode();

        Long activeProfileId = 2L;
        ActiveMode activeMode = ActiveMode.ROLE_HOST;


        dmService.checkUserParticipantStatus(request.getRoomId(), activeProfileId, activeMode);

        ResponseSendDmDto result = dmService.sendMessage(
            activeProfileId, activeMode, request
        );
        return ResponseEntity.ok(CommonResponse.ok(result, "DM 전송 완료"));
    }

    /**
     * 지정된 DM 채팅방의 메시지 목록을 조회합니다.
     *
     * <p>페이지네이션을 지원하며, 기본적으로 최신 메시지부터 조회합니다.</p>
     *
     * @param roomId 조회할 채팅방 ID
     * @param page   조회할 페이지 번호 (기본값: 0)
     * @param size   한 페이지당 조회할 메시지 수 (기본값: 20)
     * @return 메시지 목록과 상태 메시지를 포함한 {@link ResponseEntity} 객체
     * - {@link CommonResponse}로 래핑되어 전송됨
     */
    @GetMapping("{roomId}/messages")
    public ResponseEntity<CommonResponse<List<ResponseChatMessage>>> getMessages(
        @PathVariable Long roomId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        //Long activeProfileId = userDetails.getActiveProfileId();
        //ActiveMode activeMode = userDetails.getActiveMode();

        Long activeProfileId = 2L;
        ActiveMode activeMode = ActiveMode.ROLE_HOST;

        List<ResponseChatMessage> result = dmService.getMessages(roomId, page, size, activeProfileId, activeMode);
        return ResponseEntity.ok(CommonResponse.ok(result, "메시지 조회 성공"));
    }

    /**
     * DM방에서 수락/거절에 클릭에 따라 participant의 상태를 변화 시킵니다.
     *
     * @param roomId      변화 시킬 방의 ID
     * @param dto         변경 시킬 상태 DTO
     * @param userDetails 변경 시킬 유저의 ID
     * @return
     */
    @PatchMapping("/{roomId}")
    public ResponseEntity<CommonResponse<ResponseUpdateDmStatusDto>> updateDmStatus(
        @PathVariable Long roomId,
        @RequestBody RequestDmStatusDto dto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        //Long activeProfileId = userDetails.getActiveProfileId();
        //ActiveMode activeMode = userDetails.getActiveMode();

        Long activeProfileId = 2L;
        ActiveMode activeMode = ActiveMode.ROLE_HOST;

        ResponseUpdateDmStatusDto result = dmService.handleRoomStatus(roomId, dto, activeProfileId, activeMode);

        return ResponseEntity.ok(CommonResponse.ok(result, "상태 변경 완료"));


    }
}
