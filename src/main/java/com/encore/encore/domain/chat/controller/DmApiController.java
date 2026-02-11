package com.encore.encore.domain.chat.controller;

import com.encore.encore.domain.chat.dto.dm.ResponseListDmDto;
import com.encore.encore.domain.chat.service.DmService;
import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.global.common.CommonResponse;
import com.encore.encore.global.config.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Transactional
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/dm")
public class DmApiController {

    private final DmService dmService;

    @GetMapping("/pending")
    public ResponseEntity<CommonResponse<List<ResponseListDmDto>>> pending(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long activeProfileId = userDetails.getActiveProfileId();
        ActiveMode activeMode = userDetails.getActiveMode();

        List<ResponseListDmDto> result = dmService.getPending(activeProfileId, activeMode);

        return ResponseEntity.ok(CommonResponse.ok(result, "요청 받은 DM을 불러왔습니다."));

    }
}
