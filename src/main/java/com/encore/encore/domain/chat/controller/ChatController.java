package com.encore.encore.domain.chat.controller;

import com.encore.encore.domain.chat.service.ChatService;
import com.encore.encore.domain.performance.entity.Performance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        Performance performance = chatService.getPerformanceForChatPost(performanceId);

        model.addAttribute("performanceId", performanceId);
        return "/chat/chatPostForm";
    }

}
