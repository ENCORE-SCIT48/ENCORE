package com.encore.encore.domain.chat.controller;

import com.encore.encore.domain.chat.dto.ChatPostCreateRequestDTO;
import com.encore.encore.domain.chat.service.ChatService;
import com.encore.encore.global.common.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Controller
@Slf4j
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    /**
     * 채팅 글 작성 페이지 이동
     *
     * @param model
     * @return
     */
    @GetMapping("/post")
    public String post(
        @RequestParam Long performanceId,
        Model model) {
        // 공연 조회는 공연상세페이지 구현 후 연결
        // Performance performance = chatService.getPerformanceForChatPost(performanceId);

        model.addAttribute("performanceId", performanceId);
        return "/chat/chatPostForm";
    }

    @PostMapping("/chat/post")
    public CommonResponse<Void> createChatPost(
        @RequestBody ChatPostCreateRequestDTO dto
    ) {
        chatService.createChatPost(dto);
        return CommonResponse.ok(null, "성공");
    }


}
