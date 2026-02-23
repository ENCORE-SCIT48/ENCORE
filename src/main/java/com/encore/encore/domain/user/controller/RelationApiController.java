package com.encore.encore.domain.user.controller;

import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.user.dto.ResponseFollowDto;
import com.encore.encore.domain.user.dto.ResponseFollowListDto;
import com.encore.encore.domain.user.service.RelationService;
import com.encore.encore.global.common.CommonResponse;
import com.encore.encore.global.config.CustomUserDetails;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class RelationApiController {

    private final RelationService relationService;

    /**
     * 특정 프로필에 대한 팔로우 상태를 토글(팔로우/언팔로우)한다.
     *
     * <ul>
     *     <li>기존 관계가 없으면 → 새로 생성 (팔로우)</li>
     *     <li>기존 관계가 있고 isDeleted = true → 복구 (팔로우)</li>
     *     <li>기존 관계가 있고 isDeleted = false → 논리 삭제 (언팔로우)</li>
     * </ul>
     *
     * @param userDetails       로그인한 사용자의 인증 정보
     *                          (현재 활성 프로필 ID 및 모드 포함)
     * @param targetProfileId   팔로우 대상 프로필 ID
     * @param targetProfileMode 팔로우 대상 프로필 타입 (USER, PERFORMER, HOST 등)
     * @return 팔로우 상태가 반영된 {@link ResponseFollowDto}를 포함한
     * {@link CommonResponse} 객체
     */
    @PostMapping("/{targetProfileId}/{targetProfileMode}/follow")
    public ResponseEntity<CommonResponse<ResponseFollowDto>> follow(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long targetProfileId,
        @PathVariable String targetProfileMode
    ) {
        Long profileId = userDetails.getActiveProfileId();
        ActiveMode profileMode = userDetails.getActiveMode();


        if (profileId.equals(targetProfileId) && profileMode.name().equals(targetProfileMode)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "자기 자신은 팔로우 할 수 없습니다.");
        }

        ResponseFollowDto result = relationService.userFollow(
            profileId, profileMode, targetProfileId, targetProfileMode
        );

        return ResponseEntity.ok(CommonResponse.ok(result, "팔로우가 갱신 되었습니다."));
    }

    /**
     * 팔로잉 리스트를 확인할 수 있습니다.
     *
     * @param userDetails       로그인 중인 유저의 정보
     * @param targetProfileId   팔로잉 리스트를 조회할 대상의 프로필id
     * @param targetProfileMode 팔로잉 리스트를 조회할 대상의 프로필 모드
     * @return
     */
    @GetMapping("/{targetProfileId}/{targetProfileMode}/following")
    public ResponseEntity<CommonResponse<List<ResponseFollowListDto>>> getFollowingList(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long targetProfileId,
        @PathVariable String targetProfileMode
    ) {

        ActiveMode targetMode;
        try {
            targetMode = ActiveMode.valueOf(targetProfileMode.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "잘못된 profileMode 값입니다.");
        }

        Long loginProfileId = userDetails.getActiveProfileId();
        ActiveMode loginProfileMode = userDetails.getActiveMode();

        List<ResponseFollowListDto> result =
            relationService.getFollowingList(
                targetProfileId,
                targetMode,
                loginProfileId,
                loginProfileMode
            );

        return ResponseEntity.ok(CommonResponse.ok(result, "팔로잉 리스트 조회 완료"));
    }

    /**
     * 특정 프로필의 팔로워 리스트 조회
     *
     * @param targetProfileId   조회 대상 프로필 ID
     * @param targetProfileMode 조회 대상 프로필 모드 (USER, PERFORMER, HOST)
     * @param userDetails       로그인 사용자 정보
     * @return 팔로워 리스트 DTO
     */
    @GetMapping("/{targetProfileId}/{targetProfileMode}/follower")
    public ResponseEntity<CommonResponse<List<ResponseFollowListDto>>> getFollowerList(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long targetProfileId,
        @PathVariable String targetProfileMode) {

        ActiveMode targetMode;
        try {
            targetMode = ActiveMode.valueOf(targetProfileMode.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "잘못된 profileMode 값입니다.");
        }

        Long loginProfileId = userDetails.getActiveProfileId();
        ActiveMode loginProfileMode = userDetails.getActiveMode();

        List<ResponseFollowListDto> followers = relationService.getFollowerList(
            targetProfileId,
            targetMode,
            loginProfileId,
            loginProfileMode
        );

        return ResponseEntity.ok(CommonResponse.ok(followers, "팔로워 리스트 조회 성공"));
    }

}
