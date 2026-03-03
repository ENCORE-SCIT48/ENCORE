package com.encore.encore.domain.member.controller;

import com.encore.encore.domain.member.dto.MemberProfileDto;
import com.encore.encore.domain.member.dto.RecentActivitiesDto;
import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.member.service.MemberService;
import com.encore.encore.domain.user.entity.User;
import com.encore.encore.domain.user.service.RelationService;
import com.encore.encore.global.config.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RequiredArgsConstructor
@Controller
@Slf4j
public class MemberPageController {


    private final MemberService memberService;
    private final RelationService relationService;

    /**
     * 개인 페이지로 이동 한다.
     *
     * @param profileId   이동할 개인페이지의 프로필id
     * @param profileMode 이동할 개인페이지의 프로필모드
     * @param model
     * @return
     */
    @GetMapping("member/profile/{profileId}/{profileMode}")
    public String memberProfile(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long profileId,
        @PathVariable String profileMode,
        Model model
    ) {
        MemberProfileDto dto = memberService.getMemberProfileInfo(profileId, profileMode);
        List<RecentActivitiesDto> recentActivitiesDto = memberService.getRecentActivities(profileId, profileMode);

        boolean isFollowing = false;
        boolean iBlockedHim = false;
        boolean heBlockedMe = false;
        boolean isOwner = false;


        if (userDetails != null) {
            Long loginProfileId = userDetails.getActiveProfileId();
            ActiveMode loginProfileMode = userDetails.getActiveMode();
            Long loginUserId = userDetails.getUser().getUserId();

            ActiveMode targetProfileMode = ActiveMode.valueOf(profileMode);

            // 로그인된 유저와 해당 유저가 차단 관계인지 상호 조회
            iBlockedHim = relationService.isBlocked(loginUserId, loginProfileMode, profileId, targetProfileMode); // 내가 차단했나?

            // 관계를 조회하기 위해 타겟의 유저 정보 반환
            User targetUser = relationService.findProfileById(profileId, ActiveMode.valueOf(profileMode));

            heBlockedMe = relationService.isBlocked(targetUser.getUserId(), targetProfileMode, loginProfileId, loginProfileMode); // 그가 나를 차단했나?

            isOwner = (loginProfileId != null) && loginProfileId.equals(profileId) && loginProfileMode.name().equals(profileMode);

            isFollowing = relationService.isFollowing(loginUserId, loginProfileMode, profileId, ActiveMode.valueOf(profileMode));


        }

        model.addAttribute("isFollowing", isFollowing);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("user", dto);
        model.addAttribute("profileId", profileId);
        model.addAttribute("profileMode", profileMode);
        model.addAttribute("iBlockedHim", iBlockedHim);
        model.addAttribute("heBlockedMe", heBlockedMe);
        model.addAttribute("recentActivities", recentActivitiesDto);

        return "member/memberProfile";
    }
}
