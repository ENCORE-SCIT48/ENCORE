package com.encore.encore.domain.chat.controller;

import com.encore.encore.domain.chat.dto.ResponseDetailChatPostDto;
import com.encore.encore.domain.chat.dto.ResponseListChatPostDto;
import com.encore.encore.domain.chat.dto.ResponseParticipantDto;
import com.encore.encore.domain.chat.entity.ChatPost;
import com.encore.encore.domain.chat.service.ChatService;
import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.global.config.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 공연 채팅 화면(목록·상세·작성폼 등) 페이지 컨트롤러.
 * <p>
 * 공연 본 뒤 이어지는 공간(후기/택시/뒤풀이 등) 기획에 따라,
 * 목록·상세에서 채팅 유형(postType)이 노출됩니다.
 * </p>
 */
@RequiredArgsConstructor
@Controller
@Slf4j
public class ChatPageController {

    private final ChatService chatService;

    /**
     * 해당 공연 소속 채팅 게시글 작성 폼 페이지. performanceId로 공연명 조회 후 폼에 전달.
     */
    @GetMapping("/performances/{performanceId}/chats/new")
    public String post(
        @PathVariable Long performanceId,
        Model model) {
        log.info("채팅 게시글 작성 폼 - performanceId={}", performanceId);
        String performanceTitle = chatService.getPerformanceTitle(performanceId);
        model.addAttribute("performanceTitle", performanceTitle);
        model.addAttribute("performanceId", performanceId);
        return "chat/chatPostForm";
    }

    /**
     * 공연별 채팅방 목록 페이지. 전체 톡방 정보 + 개별 모집방 목록(페이징)을 모델에 담아 반환.
     * chatList 항목에 postType, postTypeDisplayName(후기·감상/택시 동승/뒤풀이/일반) 포함.
     */
    @GetMapping("/performances/{performanceId}/chats")
    public String chatListPage(@PathVariable Long performanceId, Model model,
                               @PageableDefault(size = 10) Pageable pageable) {
        log.info("공연 채팅 목록 페이지 - performanceId={}", performanceId);

        ChatPost performanceAllChatPost = chatService.findPerformanceAllChatPost(performanceId);

        model.addAttribute("performanceAllChatPost", performanceAllChatPost);
        model.addAttribute("performanceId", performanceId);
        model.addAttribute("performanceTitle", chatService.getPerformanceTitle(performanceId));

        Slice<ResponseListChatPostDto> chatList = chatService.getChatPostList(performanceId, "title", null, false, pageable);
        model.addAttribute("chatList", chatList.getContent());

        return "chat/chatPerformanceList";
    }


    /**
     * 채팅 게시글 상세 페이지. 상세 DTO에 postType/postTypeDisplayName 포함.
     *
     * @param performanceId 공연 ID (path)
     * @param id            게시글 ID (path)
     * @return chat/chatPostDetail 뷰
     */
    @GetMapping("/performances/{performanceId}/chat/{id}")
    public String chatPostDetail(
        @PathVariable Long performanceId,
        @PathVariable Long id, Model model,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("채팅방 상세 조회 진입 - performanceId: {}, postId: {}", performanceId, id);


        try {
            if (userDetails == null) {
                // 로그인 안 됐으면 로그인 페이지로 리다이렉트
                return "redirect:/auth/login";
            }
            Long activeProfileId = userDetails.getActiveProfileId(); // 현재 프로필 ID
            ActiveMode activeMode = userDetails.getActiveMode();

            ResponseDetailChatPostDto dto = chatService.getChatPostDetail(id, performanceId);
            Long roomId = chatService.getChatRoomId(id);

            boolean isWriter = chatService.canEdit(activeProfileId, activeMode, dto);


            model.addAttribute("isWriter", isWriter);

            List<ResponseParticipantDto> chatParticipantList = chatService.getChatParticipants(roomId);

            boolean canJoin = true;
            if (dto.getStatus().equals(ChatPost.Status.CLOSED.name())) {
                canJoin = chatService.canJoin(activeProfileId, activeMode, chatParticipantList);
            }
            model.addAttribute("canJoin", canJoin);
            model.addAttribute("currentProfileId", activeProfileId); // Long 타입으로 일치
            model.addAttribute("currentProfileMode", activeMode); // 타입 일치

            model.addAttribute("participantIds", chatParticipantList);
            model.addAttribute("roomId", roomId);
            model.addAttribute("chatPost", dto);
            return "chat/chatPostDetail";
        } catch (Exception e) {
            log.error("채팅 상세 조회 중 예외 발생! postId: {}", id, e);
            return "redirect:/performances/" + performanceId + "/chats";
        }
    }

    /**
     * 글 수정 페이지 이동
     *
     * @param id            글의 id
     * @param performanceId 공연 id
     * @param model         수정할 정보 저장
     * @return chat/chatPostUpdateForm.html 이동
     */
    @GetMapping("/performances/{performanceId}/chats/{id}/edit")
    public String update(
        @PathVariable Long id,
        @PathVariable Long performanceId,
        Model model
    ) {
        log.info("게시글 수정 폼 진입 - performanceId: {}, postId: {}", performanceId, id);

        try {
            ResponseDetailChatPostDto dto = chatService.getChatPostDetail(id, performanceId);

            model.addAttribute("performanceTitle", chatService.getPerformanceTitle(performanceId));
            model.addAttribute("performanceId", performanceId);
            model.addAttribute("chatPost", dto);
            return "chat/chatPostUpdateForm";
        } catch (Exception e) {
            log.error("수정 폼 로딩 중 예외 발생 postId: {}", id, e);
            return "redirect:/performances/" + performanceId + "/chat/" + id;
        }
    }

    /**
     * 푸터 채팅방 이동 페이지
     *
     * @return chat/chatJoinList.html
     */
    @GetMapping("/chats")
    public String chatList(
    ) {

        return "chat/chatJoinList";
    }

    /**
     * 참여중인 모든 채팅방 목록
     *
     * @return chat/chatJoinListFull.html
     */

    @GetMapping("/chats/join")
    public String chatListJoin(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            // 로그인 안 됐으면 로그인 페이지로 리다이렉트
            return "redirect:/auth/login";
        }
        return "chat/chatJoinListFull";
    }

    /**
     * 채팅방 페이지로 이동합니다.
     * <p>
     * 사용자가 해당 채팅방에 이미 참가자인지 확인하고,
     * 참가하지 않은 경우에는 자동으로 참가자로 추가합니다.
     * </p>
     *
     * @param roomId      조회할 채팅방 ID
     * @param model       View에 전달할 데이터를 담는 Model 객체
     * @param userDetails 현재 로그인한 사용자의 CustomUserDetails
     * @return 채팅방 페이지 이름 ("chat/chatRoom")
     */
    @GetMapping("/chat/{roomId}")
    public String chatRoom(
        @PathVariable("roomId") Long roomId,
        Model model,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }
        Long activeProfileId = userDetails.getActiveProfileId(); // 현재 프로필 ID
        ActiveMode activeMode = userDetails.getActiveMode();

        chatService.getChatAlreadJoin(roomId, activeProfileId, activeMode);

        model.addAttribute("roomId", roomId);
        model.addAttribute("currentProfileId", activeProfileId);
        model.addAttribute("currentMode", activeMode.name());
        return "chat/chatRoom";
    }
}
