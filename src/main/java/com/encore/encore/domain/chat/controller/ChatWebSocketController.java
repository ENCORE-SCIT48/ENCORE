package com.encore.encore.domain.chat.controller;

import com.encore.encore.domain.chat.dto.RequestChatMessage;
import com.encore.encore.domain.chat.dto.ResponseChatMessage;
import com.encore.encore.domain.chat.dto.UserDto;
import com.encore.encore.domain.chat.dto.dm.RequestSendDmDto;
import com.encore.encore.domain.chat.dto.dm.ResponseSendDmDto;
import com.encore.encore.domain.chat.entity.ChatParticipant;
import com.encore.encore.domain.chat.service.ChatMessageService;
import com.encore.encore.domain.chat.service.ChatService;
import com.encore.encore.domain.chat.service.DmService;
import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.global.config.CustomUserDetails;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
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
        @Payload RequestChatMessage request,
        Principal principal
    ) {

        if (principal == null) {
            log.error("인증된 사용자 정보가 없습니다.");
            throw new ApiException(ErrorCode.NOT_FOUND, "인증 정보가 없습니다.");
        }

        Authentication auth = (Authentication) principal;
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

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
        @Payload RequestSendDmDto request,
        Principal principal
    ) {
        log.info("🔥 sendDm 진입");
        log.info("principal: {}", principal);

        if (principal == null) {
            log.error("인증된 사용자 정보가 없습니다.");
            throw new ApiException(ErrorCode.NOT_FOUND, "인증 정보가 없습니다.");
        }

        Authentication auth = (Authentication) principal;
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        Long activeProfileId = userDetails.getActiveProfileId();
        ActiveMode activeMode = userDetails.getActiveMode();

        // 기존 서비스 재사용
        ResponseSendDmDto result = dmService.sendMessage(activeProfileId, activeMode, request);

        ChatParticipant other = dmService.getOtherParticipantForWebSocket(roomId, activeProfileId, activeMode);

        UserDto otherUser = dmService.getUserDto(other.getProfileId(), other.getProfileMode());

        // 상대방에게 WebSocket 전송
        messagingTemplate.convertAndSendToUser(
            otherUser.email(),
            "/queue/dm/" + roomId,
            result
        );

        // 나에게 전송
        result.setMine(true);
        // 컨트롤러 내부
        log.info("나의 Username (Principal): {}", principal.getName());
        log.info("UserDetails Username: {}", userDetails.getUsername());
        log.info("상대방 Email: {}", otherUser.email());

// 나에게 전송 시 principal.getName()을 사용해 보세요.
        messagingTemplate.convertAndSendToUser(
            principal.getName(), "/queue/dm/" + roomId,
            result);
        log.info("==== DM WebSocket 전송 시작 ====");
        log.info("보내는 대상 username: {}", principal.getName());
        log.info("roomId: {}", roomId);
        log.info("================================");
    }

}
