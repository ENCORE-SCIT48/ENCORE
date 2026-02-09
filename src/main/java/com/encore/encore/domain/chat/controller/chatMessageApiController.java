package com.encore.encore.domain.chat.controller;

import com.encore.encore.domain.chat.dto.RequestChatMessage;
import com.encore.encore.domain.chat.dto.ResponseChatMessage;
import com.encore.encore.domain.chat.service.ChatMessageService;
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
    ) {

        Long userId = 1L;

        ResponseChatMessage result = chatMessageService.sendMessage(
            roomId, userId, request
        );
        return ResponseEntity.ok(CommonResponse.ok(result, "메시지 전송 성공"));
    }

    // 메시지 조회
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<CommonResponse<List<ResponseChatMessage>>> getMessages(
        @PathVariable Long roomId
    ) {
        List<ResponseChatMessage> result = chatMessageService.getMessages(roomId);
        return ResponseEntity.ok(CommonResponse.ok(result, "메시지 조회 성공"));
    }


}
