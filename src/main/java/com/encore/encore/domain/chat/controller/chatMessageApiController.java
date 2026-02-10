package com.encore.encore.domain.chat.controller;

import com.encore.encore.domain.chat.dto.RequestChatMessage;
import com.encore.encore.domain.chat.dto.ResponseChatExitDto;
import com.encore.encore.domain.chat.dto.ResponseChatMessage;
import com.encore.encore.domain.chat.dto.ResponseParticipantDto;
import com.encore.encore.domain.chat.service.ChatMessageService;
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
@RequestMapping("/api/chat/room")
public class chatMessageApiController {

    private final ChatMessageService chatMessageService;

    /**
     * 메시지 전송
     *
     * @param roomId
     * @param request
     * @return
     */
    @PostMapping("/{roomId}/messages")
    public ResponseEntity<CommonResponse<ResponseChatMessage>> sendMessage(
        @PathVariable Long roomId,
        @RequestBody RequestChatMessage request
        //@AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        //Long activeProfileId = userDetails.getActiveProfileId();
        //ActiveMode activeMode = userDetails.getActiveMode();

        Long activeProfileId = 2L;
        ActiveMode activeMode = ActiveMode.USER;

        ResponseChatMessage result = chatMessageService.sendMessage(
            roomId, activeProfileId, activeMode, request
        );
        return ResponseEntity.ok(CommonResponse.ok(result, "메시지 전송 성공"));
    }

    /**
     * 메시지 조회
     *
     * @param roomId
     * @return
     */
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<CommonResponse<List<ResponseChatMessage>>> getMessages(
        @PathVariable Long roomId
    ) {
        List<ResponseChatMessage> result = chatMessageService.getMessages(roomId);
        return ResponseEntity.ok(CommonResponse.ok(result, "메시지 조회 성공"));
    }

    /**
     * [설명] 사용자를 채팅방에서 퇴장 처리합니다.
     *
     * @param roomId 퇴장할 채팅방 ID
     * @return 성공 메시지를 담은 응답 객체
     */
    @PostMapping("/{roomId}/exit")
    public ResponseEntity<CommonResponse<ResponseChatExitDto>> exitChat(
        @PathVariable Long roomId
        //@AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        //Long activeProfileId = userDetails.getActiveProfileId();
        //ActiveMode activeMode = userDetails.getActiveMode();

        Long activeProfileId = 3L;
        ActiveMode activeMode = ActiveMode.USER;

        log.info("[API] 채팅방 퇴장 요청 - roomId: {}, userId: {}", roomId, activeProfileId, activeMode);

        ResponseChatExitDto result = chatMessageService.leaveChat(roomId, activeProfileId, activeMode);

        return ResponseEntity.ok(CommonResponse.ok(result, "채팅방에서 성공적으로 퇴장했습니다."));
    }

    /**
     * 채팅방에 참가중인 참가자의 목록을 불러옴
     *
     * @param roomId
     * @return
     */
    @GetMapping("/{roomId}/participants")
    public ResponseEntity<CommonResponse<List<ResponseParticipantDto>>> participants(
        @PathVariable Long roomId
        //@AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        //Long activeProfileId = userDetails.getActiveProfileId();
        //ActiveMode activeMode = userDetails.getActiveMode();

        List<ResponseParticipantDto> result = chatMessageService.getParticipantList(roomId);

        return ResponseEntity.ok(CommonResponse.ok(result, "참여자 불러오기 성공"));
    }

}
