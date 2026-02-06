package com.encore.encore.domain.chat.controller;

import com.encore.encore.domain.chat.dto.*;
import com.encore.encore.domain.chat.service.ChatService;
import com.encore.encore.global.common.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatController {
    private final ChatService chatService;


    /**
     * 채팅방과 게시글을 생성
     *
     * @param performanceId 공연 ID
     * @param dto           생성 요청 정보
     * @return 생성된 게시글 정보와 성공 메시지
     */
    @PostMapping("/performance/{performanceId}/chat/post")
    @ResponseBody
    public ResponseEntity<CommonResponse<ResponseCreateChatPostDto>> createChatPost(
        @PathVariable Long performanceId,
        @Valid @RequestBody RequestCreateChatPostDto dto
    ) {
        log.info("채팅방 생성 요청 시작 - performanceId: {}", performanceId);

        ResponseCreateChatPostDto result = chatService.createChatPostAndRoom(dto, performanceId);

        log.info("채팅방 생성 완료 - postId: {}", result.getPostId());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CommonResponse.ok(result, "채팅방 생성 완료"));
    }


    /**
     * 글 논리삭제
     *
     * @param id 삭제할 글 id
     * @return 삭제한 글 정보
     */
    @DeleteMapping("/chat/{id}")
    @ResponseBody
    public ResponseEntity<CommonResponse<ResponseDeleteChatPostDto>> deletePost(@PathVariable Long id) {
        log.info("게시글 삭제 요청 - postId: {}", id);

        Long loginProfileId = 1L;
        ResponseDeleteChatPostDto result = chatService.softDeletePost(id, loginProfileId);

        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.ok(result, "삭제 성공"));
    }

    /**
     * 수정 요청 처리
     *
     * @param performanceId 수정 요청하는 글이 있는 공연 id
     * @param chatId        수정 요청 글 id
     * @param updateDTO     수정 요청 정보dto
     * @return 수정 완료 정보
     */
    @PostMapping("/performance/{performanceId}/chat/{chatId}/update")
    @ResponseBody // JSON 응답을 위해 추가
    public ResponseEntity<CommonResponse<ResponseUpdateChatPostDto>> updatePost(
        @PathVariable Long performanceId,
        @PathVariable Long chatId,
        @RequestBody RequestUpdateChatPostDto updateDTO
    ) {
        log.info("게시글 수정 요청 - chatId: {}, updateDTO: {}", chatId, updateDTO);

        Long loginProfileId = 1L;

        ResponseUpdateChatPostDto result = chatService.updateChatPost(chatId, updateDTO, loginProfileId);

        log.info("게시글 수정 성공 - chatId: {}", chatId);

        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.ok(result, "수정이 완료되었습니다."));
    }
}
