package com.encore.encore.domain.chat.controller;

import com.encore.encore.domain.chat.service.DmService;
import com.encore.encore.domain.member.entity.ActiveMode;
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
     * DM방 이동
     *
     * @param roomId
     * @param model
     * @return
     */
    @GetMapping("/dm/{roomId}")
    public String dmRoomPage(
        @PathVariable Long roomId,
        //@AuthenticationPrincipal CustomUserDetails userDetails,
        Model model) {
        // Long myProfileId = userDetails.getActiveProfileId();
        //ActiveMode myMode = userDetails.getActiveMode();

        Long activeProfileId = 3L; // 현재 프로필 ID
        ActiveMode activeMode = ActiveMode.USER;

        String participantStatus = dmService.checkUserParticipantStatus(roomId, activeProfileId, activeMode);

        model.addAttribute("participantStatus", participantStatus);
        model.addAttribute("roomId", roomId);

        return "chat/dm/dmRoom";
    }

}
