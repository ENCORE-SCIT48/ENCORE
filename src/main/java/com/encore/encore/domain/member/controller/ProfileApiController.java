package com.encore.encore.domain.member.controller;

import com.encore.encore.domain.member.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileApiController {

    private final ProfileService profileService;

}
