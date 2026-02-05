package com.encore.encore.domain.chat.controller;

import com.encore.encore.domain.chat.dto.ChatPostCreateRequestDto;
import com.encore.encore.domain.chat.dto.ChatPostResponseDto;
import com.encore.encore.domain.chat.dto.ChatPostUpdateRequestDto;
import com.encore.encore.domain.chat.service.ChatService;
import com.encore.encore.global.common.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatController {
    private final ChatService chatService;

    /**
     * 채팅방+글 생성
     *
     * @param dto
     * @return
     */
    /**
     * @PostMapping("/performance/{performanceId}/chat/post") public String createChatPost(
     * @PathVariable Long performanceId,
     * @Valid ChatPostCreateRequestDto dto,
     * BindingResult bindingResult,
     * Model model
     * /*TODO:프로필 생성 기능 완성 후 구현
     * @AuthenticationPrincipal CustomUserDetails userDetails
     * ) {
     * log.debug("[받은 데이터 확인]");
     * model.addAttribute("performanceId", performanceId);
     * log.info("채팅방 생성 요청 시작 - performanceId: {}, dto: {}", performanceId, dto);
     * <p>
     * if (bindingResult.hasErrors()) {
     * log.error("검증 에러 발생 - 필드 에러 개수: {}", bindingResult.getErrorCount());
     * return "chat/chatPostForm";
     * }
     * try {
     * chatService.createChatPostAndRoom(dto);
     * log.info("채팅방 생성 성공 - title: {}", dto.getTitle());
     * return "redirect:/performance/" + performanceId + "/chat/list";
     * } catch (Exception e) {
     * log.error("채팅방 생성 중 서버 에러 발생: ", e);
     * return "error/500";
     * }
     * }
     */
    @PostMapping("/performance/{performanceId}/chat/post")
    @ResponseBody
    public CommonResponse<ChatPostResponseDto> createChatPost(
        @PathVariable Long performanceId,
        @Valid @RequestBody ChatPostCreateRequestDto dto,
        BindingResult bindingResult
        /* TODO: @AuthenticationPrincipal CustomUserDetails userDetails */) {

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().stream()
                .map(e -> e.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b)
                .orElse("유효성 검사 실패");
            return CommonResponse.ok(null, errorMsg); // fail 없이 ok만 쓰는 구조
        }

        try {
            // 여기서 타입 변경!
            ChatPostResponseDto result = chatService.createChatPostAndRoom(dto);
            return CommonResponse.ok(result, "채팅방 생성 완료");
        } catch (Exception e) {
            return CommonResponse.ok(null, "채팅방 생성 중 서버 에러: " + e.getMessage());
        }
    }


    /**
     * 글 논리삭제
     *
     * @param postId
     * @return
     */
    @DeleteMapping("/chat/{id}")
    @ResponseBody
    public ResponseEntity<String> deletePost(
        @PathVariable("id") Long postId
        //TODO:@AuthenticationPrincipal 로그인 완성시 추가
    ) {
        log.info("게시글 삭제 요청 - postId: {}", postId);
        try {
            Long loginProfileId = 1L; //TODO:테스트용, 로그인 완성시 수정
            chatService.softDeletePost(postId, loginProfileId);
            log.info("게시글 삭제(논리 삭제) 성공 - postId: {}", postId);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            log.error("게시글 삭제 중 에러 발생: {}", e.getMessage());
            return ResponseEntity.status(500).body("fail");
        }
    }

    /**
     * 수정 요청 처리
     *
     * @param performanceId
     * @param chatId
     * @param updateDTO
     * @return
     */
    @PostMapping("/performance/{performanceId}/chat/{chatId}/update")
    public String updatePost(@PathVariable Long performanceId,
                             @PathVariable Long chatId,
                             @ModelAttribute ChatPostUpdateRequestDto updateDTO
                             //TODO:@AuthenticationPrincipal 로그인 완성시 추가
    ) {
        log.info("게시글 수정 요청 - chatId: {}, updateDTO: {}", chatId, updateDTO);
        try {
            Long loginProfileId = 1L; //TODO:테스트용, 로그인 완성시 수정
            chatService.updateChatPost(chatId, updateDTO, loginProfileId);
            log.info("게시글 수정 성공 - chatId: {}", chatId);
            return "redirect:/performance/" + performanceId + "/chat/" + chatId;
        } catch (Exception e) {
            log.error("게시글 수정 중 서버 에러: ", e);
            return "redirect:/error";
        }
    }
}
