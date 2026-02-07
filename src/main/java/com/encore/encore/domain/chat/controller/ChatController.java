package com.encore.encore.domain.chat.controller;

import com.encore.encore.domain.chat.dto.*;
import com.encore.encore.domain.chat.service.ChatService;
import com.encore.encore.global.common.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

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

    /**
     * 특정 공연에 종속된 채팅방 목록을 페이징하여 조회한다.
     *
     * @param performanceId 공연 ID
     * @param searchType    검색 타입 (title, titleContent)
     * @param keyword       검색어
     * @param onlyOpen      모집 중인 방만 보기 여부
     * @param pageable      페이징 정보
     * @return 페이징 처리된 채팅방 목록
     */
    @ResponseBody
    @GetMapping("/api/performance/{performanceId}/list")
    public ResponseEntity<CommonResponse<Slice<ResponseListChatPostDto>>> getChatList(
        @PathVariable Long performanceId,
        @RequestParam(defaultValue = "title") String searchType,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "true") boolean onlyOpen,
        @PageableDefault(size = 10) Pageable pageable) {

        log.info("채팅방 목록 조회 시작 - performanceId: {}, keyword: {}, onlyOpen: {}", performanceId, keyword, onlyOpen);

        Slice<ResponseListChatPostDto> result = chatService.getChatPostList(
            performanceId, searchType, keyword, onlyOpen, pageable
        );

        log.info("채팅방 목록 조회 완료 - 반환 데이터 수: {}", result.getContent().size());
        return ResponseEntity.ok(CommonResponse.ok(result, "채팅방 목록 조회가 완료되었습니다."));
    }

    /**
     * 참여하고 있는 채팅방을 최신 갱신된 순으로 limit까지 불러온다.
     *
     * @param limit 가져올 채팅방의 최대 수
     * @return 참여한 채팅방 목록
     */
    @ResponseBody
    @GetMapping("/api/chat/join")
    public ResponseEntity<CommonResponse<List<ResponseParticipantChatPostDto>>> getChatJoin(
        @RequestParam(defaultValue = "3") int limit
    ) {
        log.info("참여 채팅방 목록 조회 시작");
        Long userId = 1L;
        if (userId == null) {
            // 로그인 안 됨 -> 빈 리스트 반환
            return ResponseEntity.ok(CommonResponse.ok(Collections.emptyList(), "로그인 필요"));
        }
        List<ResponseParticipantChatPostDto> result = chatService.getChatPostJoinList(userId, limit);
        return ResponseEntity.ok(CommonResponse.ok(result, " 참여 채팅방 목록 조회 완료"));
    }

    /**
     * 핫한 채팅방 조회
     *
     * @param limit 가져올 채팅방의 최대 수
     * @return 모든 채팅방 중 가장 최근 활동이 있는 채팅방
     */
    @ResponseBody
    @GetMapping("/api/chat/hot")
    public ResponseEntity<CommonResponse<List<ResponseParticipantChatPostDto>>> getChatHot(
        @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("핫한 채팅방 목록 조회 시작");
        List<ResponseParticipantChatPostDto> result = chatService.getChatListHot(limit);
        return ResponseEntity.ok(CommonResponse.ok(result, " 핫한 채팅방 조회 완료"));

    }

    /**
     * 로그인한 사용자가 참여 중인 모든 채팅방 목록을 조회한다.
     * <p>
     * 페이지네이션 기반으로 채팅방 목록을 반환하며,
     * 검색어 및 검색 타입을 통해 채팅방을 필터링할 수 있다.
     *
     * @param page       조회할 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size       한 페이지당 조회할 채팅방 개수 (기본값: 20)
     * @param keyword    검색어 (null 또는 공백일 경우 전체 조회)
     * @param searchType 검색 기준 (예: title, content 등 / 기본값: title)
     * @param onlyMine   본인이 작성한 채팅방만 조회할지 여부
     *                   (true: 본인 작성 채팅방만, false: 참여 중인 모든 채팅방)
     * @return 참여 중인 채팅방 목록을 담은 {@link Slice} 형태의 응답 객체
     */
    @ResponseBody
    @GetMapping("/api/chat/join/full")
    public ResponseEntity<CommonResponse<Slice<ResponseParticipantChatPostDto>>> getChatJoin(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "title") String searchType,
        @RequestParam(defaultValue = "false") boolean onlyMine
    ) {
        Long userId = 1L; // 로그인 사용자 ID

        page = Math.max(page, 0);
        size = Math.max(size, 1);

        if (keyword != null && keyword.isBlank()) keyword = null;
        Slice<ResponseParticipantChatPostDto> result = chatService.getChatPostJoinListFull(
            userId, page, size, keyword, searchType
        );
        return ResponseEntity.ok(CommonResponse.ok(result, "참여 채팅방 목록 조회 완료"));
    }

}
