package com.encore.encore.domain.chat.controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Transactional
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/dm/room")
public class DmApiController {

}
