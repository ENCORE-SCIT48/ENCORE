package com.encore.encore.domain.chat.controller;

import com.encore.encore.domain.chat.dto.RequestChatMessage;
import com.encore.encore.domain.chat.dto.ResponseChatMessage;
import com.encore.encore.domain.chat.dto.dm.RequestSendDmDto;
import com.encore.encore.domain.chat.dto.dm.ResponseSendDmDto;
import com.encore.encore.domain.chat.entity.ChatParticipant;
import com.encore.encore.domain.chat.service.ChatMessageService;
import com.encore.encore.domain.chat.service.ChatService;
import com.encore.encore.domain.chat.service.DmService;
import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.global.config.CustomUserDetails;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService; // 기존 서비스 재사용
    private final ChatMessageService chatMessageService;
    private final DmService dmService;

    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate, ChatService chatService,
                                   ChatMessageService chatMessageService, DmService dmService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
        this.chatMessageService = chatMessageService;
        this.dmService = dmService;
    }

    @MessageMapping("/chat/{roomId}") // 클라이언트 발송 경로
    public void sendChat(
        @DestinationVariable Long roomId,
        RequestChatMessage request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long activeProfileId = userDetails.getActiveProfileId();
        ActiveMode activeMode = userDetails.getActiveMode();

        ResponseChatMessage result = chatMessageService.sendMessage(
            roomId, activeProfileId, activeMode, request
        );

        messagingTemplate.convertAndSend("/topic/chat/" + roomId, result);
    }

    @MessageMapping("/dm/{roomId}")
    public void sendDm(
        @DestinationVariable Long roomId,
        RequestSendDmDto request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long activeProfileId = userDetails.getActiveProfileId();
        ActiveMode activeMode = userDetails.getActiveMode();

        // 기존 서비스 재사용
        dmService.checkUserParticipantStatus(roomId, activeProfileId, activeMode);
        ResponseSendDmDto result = dmService.sendMessage(activeProfileId, activeMode, request);

        ChatParticipant other = dmService.getOtherParticipantForWebSocket(roomId, activeProfileId, activeMode);

        // WebSocket 전송
        messagingTemplate.convertAndSendToUser(
            other.getProfileId().toString(),
            "/queue/dm/" + roomId,
            result
        );
    }
}
