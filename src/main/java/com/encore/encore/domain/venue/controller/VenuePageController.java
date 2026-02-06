package com.encore.encore.domain.venue.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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
}
