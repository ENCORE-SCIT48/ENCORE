package com.encore.encore.domain.feed.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/feed")
public class FeedPageController {

    @GetMapping
    public String feedPage(Model model) {

        List<Map<String, String>> footerItems = List.of(
            Map.of("label", "공연리스트", "href", "/performances"),
            Map.of("label", "홈", "href", "/feed"),
            Map.of("label", "DM", "href", "/dm/list"),
            Map.of("label", "채팅방", "href", "/chats")
        );

        model.addAttribute("footerItems", footerItems);
        return "feed/list";
    }
}
