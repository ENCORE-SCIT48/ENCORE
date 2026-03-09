package com.encore.encore.domain.member.service;

import com.encore.encore.domain.member.dto.PerformerProfileRequestDto;
import com.encore.encore.domain.member.entity.PerformerProfile;
import com.encore.encore.domain.member.repository.PerformerProfileRepository;
import com.encore.encore.domain.user.entity.User;
import com.encore.encore.global.common.service.FileService;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerformerProfileService {

    private final PerformerProfileRepository performerProfileRepository;
    private final FileService fileService;

    /**
     * [퍼포머 프로필 조회] 없으면 null 반환 (프로필 선택 페이지 등에서 사용)
     */
    public PerformerProfileRequestDto getPerformerProfileOrNull(User user) {
        return performerProfileRepository.findByUser(user)
            .map(PerformerProfileRequestDto::from)
            .orElse(null);
    }

    /**
     * [퍼포머 프로필 조회]
     * 현재 로그인한 유저의 정보를 기반으로 퍼포머 프로필 엔티티를 찾아 응답 DTO로 변환하여 반환합니다.
     * @param user 현재 인증된 유저 객체 (UserDetails에서 추출한 User 엔티티)
     * @return 조회된 퍼포머 프로필 정보를 담은 DTO
     * @throws ApiException ErrorCode.NOT_FOUND: 프로필 데이터가 존재하지 않을 경우 발생
     */
    public PerformerProfileRequestDto getPerformerProfile(User user) {
        PerformerProfile profile = performerProfileRepository.findByUser(user)
            .orElseThrow(() -> {
                log.warn("[ProfileService] 퍼포머 프로필 조회 실패 - User Email: {}", user.getEmail());
                return new ApiException(ErrorCode.NOT_FOUND, "등록된 공연자 프로필이 없습니다.");
            });

        return PerformerProfileRequestDto.from(profile);
    }

    /**
     * [2] 퍼포머 프로필 업데이트 (생성 및 수정 통합)
     *
     * @param user         현재 인증된 유저 엔티티
     * @param dto          화면에서 넘어온 수정 데이터
     * @param profileImage 업로드된 파일 (Nullable)
     */
    @Transactional
    public void updatePerformerProfile(User user,
                                       PerformerProfileRequestDto dto,
                                       MultipartFile profileImage) {

        // 1. 기존 프로필 조회
        PerformerProfile profile = performerProfileRepository.findByUser(user)
            .orElseThrow(() -> {
                log.warn("[PerformerProfileService] 퍼포머 프로필 조회 실패 - User : {}", user);
                return new ApiException(ErrorCode.NOT_FOUND, "등록된 공연자 프로필이 없습니다.");
            });

        // 2. 이미지 처리
        String imageUrl = profile.getProfileImageUrl(); // 기존 URL을 기본값으로 유지

        if (profileImage != null && !profileImage.isEmpty()) {
            // 기존 파일 삭제 로직
            if (profile.getProfileImageUrl() != null) {
                log.info("[FILE_DELETE] Deleting old performer image: {}", profile.getProfileImageUrl());
                fileService.deletePhysicalFile(profile.getProfileImageUrl());
            }

            imageUrl = fileService.saveFile(profileImage);
            log.info("[FILE_UPLOAD_SUCCESS] New performer image: {}", imageUrl);
        } else {
            log.info("[PROFILE_IMAGE_KEEP] 기존 프로필 이미지 유지 - User: {}", user.getEmail());
        }
        // 3. 데이터 업데이트 (initialize 내부에서 imageUrl도 처리하게 하거나 따로 세팅)
        profile.initialize(dto, imageUrl);


        //영속 상태이므로 명시적인 save() 호출 없이 변경 감지(Dirty Checking)로 저장됨
        log.info("[PROFILE_UPDATE_SUCCESS] Performer profile updated for user: {}", user.getEmail());
    }
}
