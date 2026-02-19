package com.encore.encore.domain.member.controller;

import com.encore.encore.domain.member.dto.MemberProfileDto;
import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.member.service.MemberService;
import com.encore.encore.domain.user.service.RelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Controller
@Slf4j
public class MemberPageController {

    private final MemberService memberService;
    private final RelationService relationService;

    /**
     * 개인 페이지로 이동한다.
     *
     * @return
     */
    @GetMapping("member/profile/{profileId}/{profileMode}")
    public String memberProfile(
        //@AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long profileId,
        @PathVariable String profileMode,
        Model model
    ) {
        MemberProfileDto dto = memberService.getMemberProfileInfo(profileId, profileMode);


        //Long loginProfileId = userDetails.getActiveProfileId();
        //ActiveMode loginProfileMode = userDetails.getActiveMode();
        //Long loginUserId = userDetails.getUser().getUserId();

        Long loginProfileId = 2L;
        ActiveMode loginProfileMode = ActiveMode.HOST;
        Long loginUserId = 5L;

        boolean isOwner = (loginProfileId != null) && loginProfileId.equals(profileId) && loginProfileMode.name().equals(profileMode);

        boolean isFollowing = relationService.isFollowing(loginUserId, loginProfileMode, profileId, ActiveMode.valueOf(profileMode));

        model.addAttribute("isFollowing", isFollowing);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("user", dto);
        model.addAttribute("profileId", profileId);
        model.addAttribute("profileMode", profileMode);

        return "member/memberProfile";
    }
}
