package com.encore.encore.domain.chat.controller;

import com.encore.encore.domain.chat.service.DmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
    @GetMapping("dm/list")
    public String dmList() {

        return "chat/dm/dmList";
    }

    /**
     * DM방 이동
     *
     * @param roomId
     * @param pending
     * @param model
     * @return
     */
    @GetMapping("/dm/{roomId}")
    public String dmRoomPage(
        @PathVariable Long roomId,
        @RequestParam(required = false) Boolean pending,
        Model model) {

        model.addAttribute("roomId", roomId);
        model.addAttribute("isPending", Boolean.TRUE.equals(pending));

        return "chat/dm/dmRoom";
    }

}
