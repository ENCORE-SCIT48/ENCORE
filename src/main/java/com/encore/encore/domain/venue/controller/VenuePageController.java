package com.encore.encore.domain.venue.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/venues")
public class VenuePageController {

    @GetMapping
    public String listPage() {
        log.info("[VenuePage] list page requested");
        return "venue/list";
    }

    // 등록 모드
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "CREATE");
        return "venue/venueForm";
    }

    // 수정 및 삭제 모드
    @GetMapping("/{venueId}/edit")
    public String editForm(@PathVariable Long venueId, Model model) {
        model.addAttribute("venueId", venueId);
        model.addAttribute("mode", "UPDATE");
        return "venue/venueForm";
    }
}
