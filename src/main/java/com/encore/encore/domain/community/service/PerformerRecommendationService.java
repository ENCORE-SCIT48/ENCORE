package com.encore.encore.domain.community.service;

import com.encore.encore.domain.member.dto.ResponsePerformerRecommendDto;
import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.member.entity.PerformerProfile;
import com.encore.encore.domain.member.repository.PerformerProfileRepository;
import com.encore.encore.global.config.CustomUserDetails;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PerformerRecommendationService {

    private final PerformerProfileRepository performerProfileRepository;

    /**
     * [설명] 로그인한 공연자를 제외한 공연자 추천 목록을 조회합니다.
     *
     * - 공연자 권한(ROLE_PERFORMER)만 접근 가능합니다.
     * - 무대명(keyword), 활동 지역(activityArea), 포지션(part) 조건을 적용합니다.
     * - Pageable을 이용하여 페이징 처리합니다.
     * - 조회 결과를 ResponsePerformerRecommendDto로 변환하여 반환합니다.
     *
     * @param userDetails  로그인 사용자 정보
     * @param keyword      무대명 검색 키워드 (nullable)
     * @param activityArea 활동 지역 필터 (nullable)
     * @param part         포지션 필터 (nullable)
     * @param pageable     페이징 정보
     * @return 페이징 처리된 공연자 추천 목록
     */
    public Page<ResponsePerformerRecommendDto> getPerformerList(
            CustomUserDetails userDetails,
            String keyword,
            String activityArea,
            String part,
            Pageable pageable) {

        if (userDetails == null || userDetails.getActiveMode() != ActiveMode.ROLE_PERFORMER) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }

        Long loginUserId = userDetails.getUser().getUserId();

        Page<PerformerProfile> page = performerProfileRepository.searchWithFilter(
                loginUserId,
                keyword,
                activityArea,
                part,
                pageable);

        return page.map(profile -> ResponsePerformerRecommendDto.builder()
                .userId(profile.getUser().getUserId())
                .stageName(profile.getStageName())
                .profileImageUrl(profile.getProfileImageUrl())
                .part(profile.getPart())
                .category(profile.getCategory())
                .skillLevel(profile.getSkillLevel())
                .activityArea(profile.getActivityArea())
                .build());
    }
}
