package com.encore.encore.domain.chat.controller;

import com.encore.encore.domain.chat.dto.*;
import com.encore.encore.domain.chat.service.ChatService;
import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.global.common.CommonResponse;
import com.encore.encore.global.config.CustomUserDetails;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 공연 채팅 API 컨트롤러.
 * <p>
 * 공연별 채팅방 생성·목록 조회·수정·삭제, 참여 목록·핫 채팅 조회 등 REST API를 제공합니다.
 * 공연 채팅은 "공연 본 뒤 이어지는 공간"(후기/택시/뒤풀이 등) 기획에 따라 postType으로 구분됩니다.
 * </p>
 *
 * @see ChatService
 * @see com.encore.encore.domain.chat.entity.ChatPostType
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class ChatController {
    private final ChatService chatService;

    /**
     * 해당 공연 소속 채팅 게시글(모집글) 및 채팅방 생성.
     * <p>
     * 요청 본문에 title, content, maxMember, postType(REVIEW/TAXI_SHARE/AFTER_PARTY/GENERAL) 등을 담아 전달.
     * </p>
     *
     * @param performanceId path 공연 ID
     * @param dto           생성 요청 (제목, 내용, 모집인원, postType 등)
     * @param userDetails   로그인 사용자 (activeProfileId, activeMode 사용)
     * @return 201 + 생성된 게시글·채팅방 정보 (postId, chatRoomId, postType 표시명 포함)
     */
    @PostMapping("/api/performances/{performanceId}/chats")
    public ResponseEntity<CommonResponse<ResponseCreateChatPostDto>> createChatPost(
        @PathVariable Long performanceId,
        @Valid @RequestBody RequestCreateChatPostDto dto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("채팅방 생성 요청 시작 - performanceId: {}", performanceId);
        Long activeProfileId = userDetails.getActiveProfileId(); // 현재 프로필 ID
        ActiveMode activeMode = userDetails.getActiveMode();

        ResponseCreateChatPostDto result = chatService.createChatPostAndRoom(dto, performanceId, activeProfileId, activeMode);

        log.info("채팅방 생성 완료 - postId: {}", result.getPostId());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CommonResponse.ok(result, "채팅방 생성 완료"));
    }


    /**
     * 채팅 게시글(및 연결된 채팅방) 논리 삭제. 작성자만 가능.
     *
     * @param id          삭제할 게시글 ID
     * @param userDetails 로그인 사용자 (권한 확인용)
     * @return 삭제된 postId, chatRoomId, 삭제 여부
     */
    @DeleteMapping("/api/chat/{id}")
    public ResponseEntity<CommonResponse<ResponseDeleteChatPostDto>> deletePost(
        @PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("게시글 삭제 요청 - postId: {}", id);

        Long activeProfileId = userDetails.getActiveProfileId(); // 현재 프로필 ID
        ActiveMode activeMode = userDetails.getActiveMode();

        ResponseDeleteChatPostDto result = chatService.softDeletePost(id, activeProfileId, activeMode);

        log.info("채팅 게시글 삭제 완료 - postId={}", id);
        return ResponseEntity.ok(CommonResponse.ok(result, "삭제 성공"));
    }

    /**
     * 채팅 게시글 수정. 작성자만 가능. 제목·내용·상태(OPEN/CLOSED) 수정.
     *
     * @param performanceId path 공연 ID
     * @param chatId        path 게시글 ID
     * @param updateDTO     수정할 제목·내용·상태
     * @param userDetails   로그인 사용자 (권한 확인)
     * @return 수정된 게시글 정보
     */
    @PatchMapping("/api/performances/{performanceId}/chats/{chatId}")
    public ResponseEntity<CommonResponse<ResponseUpdateChatPostDto>> updatePost(
        @PathVariable Long performanceId,
        @PathVariable Long chatId,
        @RequestBody RequestUpdateChatPostDto updateDTO,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("채팅 게시글 수정 요청 - chatId={}, performanceId={}", chatId, performanceId);

        Long activeProfileId = userDetails.getActiveProfileId(); // 현재 프로필 ID
        ActiveMode activeMode = userDetails.getActiveMode();

        ResponseUpdateChatPostDto result = chatService.updateChatPost(chatId, updateDTO, activeProfileId, activeMode);

        log.info("채팅 게시글 수정 완료 - chatId={}", chatId);

        return ResponseEntity.ok(CommonResponse.ok(result, "수정이 완료되었습니다."));
    }

    /**
     * 특정 공연에 속한 채팅방 목록 페이징 조회. 제목/제목+내용 검색·모집중만 보기 지원.
     * 각 항목에 postType(후기·택시·뒤풀이 등) 표시명 포함.
     *
     * @param performanceId path 공연 ID
     * @param searchType    title | titleContent
     * @param keyword       검색어 (선택)
     * @param onlyOpen      true면 모집 중(OPEN)만
     * @param pageable      페이징 (기본 size=10)
     * @return Slice 형태 채팅방 목록 (postType, postTypeDisplayName 포함)
     */
    @GetMapping("/api/performances/{performanceId}/chats")
    public ResponseEntity<CommonResponse<Slice<ResponseListChatPostDto>>> getChatList(
        @PathVariable Long performanceId,
        @RequestParam(defaultValue = "title") String searchType,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "true") boolean onlyOpen,
        @PageableDefault(size = 10) Pageable pageable) {

        log.info("공연별 채팅방 목록 조회 - performanceId={}, keyword={}, onlyOpen={}", performanceId, keyword, onlyOpen);

        Slice<ResponseListChatPostDto> result = chatService.getChatPostList(
            performanceId, searchType, keyword, onlyOpen, pageable
        );

        log.info("채팅방 목록 조회 완료 - 반환 데이터 수: {}", result.getContent().size());
        return ResponseEntity.ok(CommonResponse.ok(result, "채팅방 목록 조회가 완료되었습니다."));
    }

    /**
     * 로그인 사용자가 참여 중인 채팅방을 최신순으로 limit개 조회.
     *
     * @param limit       최대 개수 (기본 3)
     * @param userDetails 로그인 사용자 (비로그인 시 빈 목록 반환)
     * @return 참여 채팅방 목록 (postType 표시명 포함)
     */
    @GetMapping("/api/chat/join")
    public ResponseEntity<CommonResponse<List<ResponseMyChatPostDto>>> getChatJoinLimit(
        @RequestParam(defaultValue = "3") int limit,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("참여 채팅방 목록 조회(limit) - limit={}", limit);
        if (userDetails == null) {
            return ResponseEntity.ok(CommonResponse.ok(Collections.emptyList(), "로그인이 필요합니다."));
        }
        Long activeProfileId = userDetails.getActiveProfileId(); // 현재 프로필 ID
        ActiveMode activeMode = userDetails.getActiveMode();

        if (activeProfileId == null) {
            // 로그인 안 됨 -> 빈 리스트 반환
            return ResponseEntity.ok(CommonResponse.ok(Collections.emptyList(), "로그인 필요"));
        }
        List<ResponseMyChatPostDto> result = chatService.getChatPostJoinList(activeProfileId, activeMode, limit);
        return ResponseEntity.ok(CommonResponse.ok(result, " 참여 채팅방 목록 조회 완료"));
    }

    /**
     * 최근 활동이 많은 채팅방(Hot) 목록 조회. 메시지 최신순으로 limit개.
     *
     * @param limit 최대 개수 (기본 10)
     * @return Hot 채팅방 목록
     */
    @GetMapping("/api/chat/hot")
    public ResponseEntity<CommonResponse<List<ResponseMyChatPostDto>>> getChatHot(
        @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("핫 채팅방 목록 조회 - limit={}", limit);
        List<ResponseMyChatPostDto> result = chatService.getChatListHot(limit);
        return ResponseEntity.ok(CommonResponse.ok(result, " 핫한 채팅방 조회 완료"));

    }

    /**
     * 로그인 사용자가 참여 중인 채팅방 전체 목록 페이징 조회.
     * 제목/내용/공연명 검색, 본인 작성만 보기(onlyMine) 지원.
     *
     * @param page       페이지 번호 (0부터, 기본 0)
     * @param size       페이지 크기 (기본 20)
     * @param keyword    검색어 (선택)
     * @param searchType title | titleContent | performanceTitle
     * @param onlyMine   true면 본인 작성 채팅방만
     * @param userDetails 로그인 사용자 (필수)
     * @return Slice 형태 참여 채팅방 목록
     */
    @GetMapping("/api/users/chats")
    public ResponseEntity<CommonResponse<Slice<ResponseMyChatPostDto>>> getChatJoin(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "title") String searchType,
        @RequestParam(defaultValue = "false") boolean onlyMine,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        Long activeProfileId = userDetails.getActiveProfileId(); // 현재 프로필 ID
        ActiveMode activeMode = userDetails.getActiveMode();

        page = Math.max(page, 0);
        size = Math.max(size, 1);

        if (keyword != null && keyword.isBlank()) keyword = null;
        Slice<ResponseMyChatPostDto> result = chatService.getChatPostJoinListFull(
            activeProfileId, activeMode, page, size, keyword, searchType, onlyMine
        );
        return ResponseEntity.ok(CommonResponse.ok(result, "참여 채팅방 목록 조회 완료"));
    }


}
