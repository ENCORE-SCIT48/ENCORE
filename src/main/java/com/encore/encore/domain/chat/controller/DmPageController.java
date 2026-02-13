package com.encore.encore.domain.chat.controller;

import com.encore.encore.domain.chat.service.DmService;
import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.global.config.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Controller
@Slf4j
public class DmPageController {

    private final DmService dmService;

    /**
     * DM 리스트 이동 컨트롤러
     *
     * @return
     */
    @GetMapping("/dm/list")
    public String dmList() {

        return "chat/dm/dmList";
    }

    /**
     * 특정 DM 채팅방 페이지를 반환합니다.
     *
     * <p>사용자의 활성 프로필과 모드를 기반으로 해당 채팅방 참여 상태를 확인하고,
     * 참여 상태와 채팅방 ID를 모델에 담아 Thymeleaf 뷰로 전달합니다.</p>
     *
     * @param roomId      조회할 DM 채팅방 ID
     * @param userDetails 현재 인증된 사용자 정보 {@link CustomUserDetails}
     * @param model       뷰로 전달할 데이터를 담는 {@link Model} 객체
     * @return DM 채팅방 뷰 이름 ("chat/dm/dmRoom")
     */
    @GetMapping("/dm/{roomId}")
    public String dmRoomPage(
        @PathVariable Long roomId,
        // @AuthenticationPrincipal CustomUserDetails userDetails,
        Model model) {

        //Long activeProfileId = userDetails.getActiveProfileId();
        //ActiveMode activeMode = userDetails.getActiveMode();

        Long activeProfileId = 2L;
        ActiveMode activeMode = ActiveMode.USER;

        String participantStatus = dmService.checkUserParticipantStatus(roomId, activeProfileId, activeMode);

        model.addAttribute("participantStatus", participantStatus);
        model.addAttribute("roomId", roomId);

        return "chat/dm/dmRoom";
    }

}
