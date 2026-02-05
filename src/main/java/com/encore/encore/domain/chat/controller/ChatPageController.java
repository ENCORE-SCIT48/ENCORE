package com.encore.encore.domain.chat.controller;

import com.encore.encore.domain.chat.dto.ChatPostDetailResponseDto;
import com.encore.encore.domain.chat.dto.ChatPostListResponseDto;
import com.encore.encore.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Controller
@Slf4j
public class ChatPageController {

    private final ChatService chatService;

    /**
     * 채팅 게시물 작성 페이지 이동
     *
     * @param model 작성 performance 저장
     * @return chatPostForm.html 페이지 이동
     */
    @GetMapping("/performance/{performanceId}/chat/post")
    public String post(
        @PathVariable Long performanceId,
        Model model) {
        log.info("채팅 게시글 작성 폼 진입 - performanceId: {}", performanceId);
        String performanceTitle = chatService.getPerformanceTitle(performanceId);
        model.addAttribute("performanceTitle", performanceTitle);
        model.addAttribute("performanceId", performanceId);
        return "chat/chatPostForm";
    }

    /**
     * 공연별 채팅방 조회 화면 이동
     *
     * @param performanceId
     * @param model
     * @return
     */
    @GetMapping("/performance/{performanceId}/chat/list")
    public String chatList(
        @PathVariable Long performanceId,
        Model model
    ) {
        log.info("공연별 채팅방 목록 조회 시작 - performanceId: {}", performanceId);

        try {
            List<ChatPostListResponseDto> chatPostList = chatService.getChatPostsByPerformance(performanceId);
            if (chatPostList == null) {
                chatPostList = new ArrayList<>();
            }

            log.info("채팅방 목록 조회 완료 - 조회된 개수: {}", chatPostList.size());
            model.addAttribute("chatPostList", chatPostList);
            model.addAttribute("performanceId", performanceId);
            return "chat/chatPerformanceList";

        } catch (Exception e) {
            // debug 대신 error를 쓰고, 예외 객체 e를 넘겨 StackTrace를 찍습니다.
            log.error("채팅방 목록 조회 중 예외 발생! performanceId: {}", performanceId, e);
            return "redirect:/";
        }
    }

    /**
     * 채팅방 글 상세사항 조회
     *
     * @param id
     * @param model
     * @return
     */
    @GetMapping("/performance/{performanceId}/chat/{id}")
    public String chatPostDetail(
        @PathVariable Long performanceId,
        @PathVariable Long id, Model model

    ) {
        log.info("채팅방 상세 조회 진입 - performanceId: {}, postId: {}", performanceId, id);

        try {
            ChatPostDetailResponseDto dto = chatService.getChatPostDetail(id);
            model.addAttribute("chatPost", dto);
            return "chat/chatPostDetail";
        } catch (Exception e) {
            log.error("채팅 상세 조회 중 예외 발생! postId: {}", id, e);
            return "redirect:/performance/" + performanceId + "/chat/list";
        }
    }

    /**
     * 글 수정 페이지 이동
     *
     * @param postId
     * @param performanceId
     * @param model
     * @return
     */
    @GetMapping("/performance/{performanceId}/chat/{id}/update")
    public String update(
        @PathVariable("id") Long postId,
        @PathVariable Long performanceId,
        Model model
    ) {
        log.info("게시글 수정 폼 진입 - performanceId: {}, postId: {}", performanceId, postId);

        try {
            ChatPostDetailResponseDto dto = chatService.getChatPostDetail(postId);
            model.addAttribute("performanceId", performanceId);
            model.addAttribute("chatPost", dto);
            return "chat/chatPostUpdateForm"; // 앞에 붙은 / 제거 (Thymeleaf 관례)
        } catch (Exception e) {
            log.error("수정 폼 로딩 중 예외 발생 postId: {}", postId, e);
            return "redirect:/performance/" + performanceId + "/chat/" + postId;
        }
    }
}
