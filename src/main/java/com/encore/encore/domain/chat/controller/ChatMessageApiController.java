package com.encore.encore.domain.chat.controller;

import com.encore.encore.domain.chat.dto.RequestChatMessage;
import com.encore.encore.domain.chat.dto.ResponseChatExitDto;
import com.encore.encore.domain.chat.dto.ResponseChatMessage;
import com.encore.encore.domain.chat.dto.ResponseParticipantDto;
import com.encore.encore.domain.chat.service.ChatMessageService;
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
@RequestMapping("/api/chat/room")
public class ChatMessageApiController {

    private final ChatMessageService chatMessageService;

    /**
     * [설명] 지정된 채팅방(roomId)에 메시지를 전송합니다.
     * <p>
     * 요청한 사용자의 프로필 ID와 모드를 기준으로 메시지를 저장하며,
     * 저장 후 메시지 정보(ResponseChatMessage)를 반환합니다.
     * </p>
     *
     * @param roomId      메시지를 전송할 채팅방 ID
     * @param request     전송할 메시지 내용 및 추가 정보가 담긴 DTO {@link RequestChatMessage}
     * @param userDetails 현재 로그인한 사용자의 인증 정보 {@link CustomUserDetails}
     * @return 메시지 전송 결과를 담은 {@link CommonResponse} 객체
     * @throws IllegalArgumentException 메시지 내용이 유효하지 않을 경우
     * @throws RuntimeException         저장 중 서버 에러 발생 시
     */
    @PostMapping("/{roomId}/messages")
    public ResponseEntity<CommonResponse<ResponseChatMessage>> sendMessage(
        @PathVariable Long roomId,
        @RequestBody RequestChatMessage request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        Long activeProfileId = userDetails.getActiveProfileId(); // 현재 프로필 ID
        ActiveMode activeMode = userDetails.getActiveMode();

        ResponseChatMessage result = chatMessageService.sendMessage(
            roomId, activeProfileId, activeMode, request
        );
        return ResponseEntity.ok(CommonResponse.ok(result, "메시지 전송 성공"));
    }

    /**
     * [설명] 지정된 채팅방(roomId)의 메시지 목록을 조회합니다.
     * <p>
     * 페이지네이션 기능을 제공하며, page, size 파라미터로 조회 범위를 지정할 수 있습니다.
     * 최신 메시지부터 정렬되어 반환됩니다.
     * </p>
     *
     * @param roomId 채팅방 ID
     * @param page   조회할 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size   한 페이지당 조회할 메시지 수 (기본값: 20)
     * @return 조회된 메시지 목록을 담은 {@link CommonResponse} 객체
     * @throws RuntimeException DB 조회 실패 등 서버 에러 발생 시
     */
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<CommonResponse<List<ResponseChatMessage>>> getMessages(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long roomId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Long activeProfileId = userDetails.getActiveProfileId(); // 현재 프로필 ID
        ActiveMode activeMode = userDetails.getActiveMode();

        List<ResponseChatMessage> result = chatMessageService.getMessages(roomId, page, size, activeProfileId, activeMode);
        return ResponseEntity.ok(CommonResponse.ok(result, "메시지 조회 성공"));
    }

    /**
     * [설명] 현재 로그인 사용자를 지정된 채팅방(roomId)에서 퇴장 처리합니다.
     * <p>
     * 퇴장 요청 시 DB에서 사용자의 채팅방 참여 상태를 변경하고,
     * 퇴장 결과 정보를 {@link ResponseChatExitDto}로 반환합니다.
     * </p>
     *
     * @param roomId      퇴장할 채팅방 ID
     * @param userDetails 현재 로그인한 사용자의 인증 정보 {@link CustomUserDetails}
     * @return 퇴장 결과를 담은 {@link CommonResponse} 객체
     * @throws IllegalStateException 채팅방에 참여하지 않은 사용자가 요청할 경우
     * @throws RuntimeException      서버 오류 발생 시
     */
    @PostMapping("/{roomId}/exit")
    public ResponseEntity<CommonResponse<ResponseChatExitDto>> exitChat(
        @PathVariable Long roomId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        Long activeProfileId = userDetails.getActiveProfileId(); // 현재 프로필 ID
        ActiveMode activeMode = userDetails.getActiveMode();

        log.info("[API] 채팅방 퇴장 요청 - roomId: {}, userId: {}", roomId, activeProfileId, activeMode);

        ResponseChatExitDto result = chatMessageService.leaveChat(roomId, activeProfileId, activeMode);

        return ResponseEntity.ok(CommonResponse.ok(result, "채팅방에서 성공적으로 퇴장했습니다."));
    }

    /**
     * [설명] 지정된 채팅방(roomId)에 참여 중인 모든 참가자의 목록을 조회합니다.
     * <p>
     * 참가자의 프로필 정보와 상태를 포함하여 {@link ResponseParticipantDto} 형태로 반환합니다.
     * </p>
     *
     * @param roomId      조회할 채팅방 ID
     * @param userDetails 현재 로그인한 사용자의 인증 정보 {@link CustomUserDetails}
     * @return 채팅방 참가자 목록을 담은 {@link CommonResponse} 객체
     * @throws RuntimeException 서버 조회 오류 발생 시
     */
    @GetMapping("/{roomId}/participants")
    public ResponseEntity<CommonResponse<List<ResponseParticipantDto>>> participants(
        @PathVariable Long roomId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long activeProfileId = userDetails.getActiveProfileId(); // 현재 프로필 ID
        ActiveMode activeMode = userDetails.getActiveMode();

        List<ResponseParticipantDto> result = chatMessageService.getParticipantList(roomId);

        return ResponseEntity.ok(CommonResponse.ok(result, "참여자 불러오기 성공"));
    }

}
