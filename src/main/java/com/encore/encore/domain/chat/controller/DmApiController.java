package com.encore.encore.domain.chat.controller;

import com.encore.encore.domain.chat.dto.dm.RequestDmDto;
import com.encore.encore.domain.chat.dto.dm.ResponseDmRoomStatusDto;
import com.encore.encore.domain.chat.dto.dm.ResponseListDmDto;
import com.encore.encore.domain.chat.service.DmService;
import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.global.common.CommonResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Transactional
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/dm")
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
        //@AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        //   Long activeProfileId = userDetails.getActiveProfileId();
        // ActiveMode activeMode = userDetails.getActiveMode();

        Long activeProfileId = 2L; // 현재 프로필 ID
        ActiveMode activeMode = ActiveMode.USER;

        List<ResponseListDmDto> result = dmService.getPendingList(activeProfileId, activeMode);

        return ResponseEntity.ok(CommonResponse.ok(result, "요청 받은 DM을 불러왔습니다."));
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
        // @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        //   Long activeProfileId = userDetails.getActiveProfileId();
        // ActiveMode activeMode = userDetails.getActiveMode();

        Long activeProfileId = 2L; // 현재 프로필 ID
        ActiveMode activeMode = ActiveMode.USER;

        List<ResponseListDmDto> result = dmService.getAcceptedList(activeProfileId, activeMode);

        return ResponseEntity.ok(CommonResponse.ok(result, "요청 받은 DM을 불러왔습니다."));
    }

    /**
     * 개인페이지에서 DM 요청 처리
     * - 기존 DM 확인 후 roomId 반환
     * - 없으면 새로 생성
     */
    @PostMapping("/request")
    public ResponseEntity<CommonResponse<ResponseDmRoomStatusDto>> requestDm(
        //@AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody RequestDmDto dto
    ) {
        // Long myProfileId = userDetails.getActiveProfileId();
        //ActiveMode myMode = userDetails.getActiveMode();

        Long activeProfileId = 3L; // 현재 프로필 ID
        ActiveMode activeMode = ActiveMode.USER;

        ResponseDmRoomStatusDto result = dmService.requestDm(activeProfileId, activeMode, dto);

        return ResponseEntity.ok(CommonResponse.ok(result, "DM 요청 처리 완료"));
    }

}
