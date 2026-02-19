package com.encore.encore.domain.member.service;

import com.encore.encore.domain.member.dto.HostProfileRequestDto;
import com.encore.encore.domain.member.dto.HostProfileResponseDto;
import com.encore.encore.domain.member.entity.HostProfile;
import com.encore.encore.domain.member.repository.HostProfileRepository;
import com.encore.encore.domain.user.entity.User;
import com.encore.encore.global.common.service.FileService;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class HostProfileService {

    private final HostProfileRepository hostProfileRepository;
    private final FileService fileService;

    public HostProfileResponseDto getHostProfile(User user) {
        // 1. DB에서 해당 유저의 호스트 프로필 조회
        HostProfile profile = hostProfileRepository.findByUser(user)
            .orElseThrow(() -> new RuntimeException("해당 유저의 호스트 프로필을 찾을 수 없습니다."));

        // 2. Entity를 Response DTO로 변환하여 반환
        return HostProfileResponseDto.from(profile);
    }

    public void updateHostProfile(User user,
                                  @Valid HostProfileRequestDto dto,
                                  MultipartFile profileImage) {

        HostProfile profile = hostProfileRepository.findByUser(user)
            .orElseThrow(() -> {
                log.warn("[HostProfileService] 퍼포머 프로필 조회 실패 - User : {}", user);
                return new ApiException(ErrorCode.NOT_FOUND, "등록된 호스트 프로필이 없습니다.");
            });

        // 1. 인증 여부 확인
        if (!profile.isVerified()) {
            // 비즈니스 예외를 던져서 컨트롤러나 GlobalExceptionHandler에서 처리하게 함
            throw new ApiException(ErrorCode.UNAUTHORIZED, "사업자 인증을 먼저 완료해주세요.");
        }
        // 2. 이미지 처리
        String imageUrl = profile.getProfileImageUrl(); // 기존 URL을 기본값으로 유지

        if (profileImage != null && !profileImage.isEmpty()) {
            // 기존 파일 삭제 로직
            if (profile.getProfileImageUrl() != null) {
                log.info("[FILE_DELETE] Deleting old Host image: {}", profile.getProfileImageUrl());
                fileService.deletePhysicalFile(profile.getProfileImageUrl());
            }

            imageUrl = fileService.saveFile(profileImage);
            log.info("[FILE_UPLOAD_SUCCESS] New Host image: {}", imageUrl);
        } else {
            log.info("[PROFILE_IMAGE_KEEP] 기존 프로필 이미지 유지 - User: {}", user.getEmail());
        }

        // 3. 데이터 업데이트 (initialize 내부에서 imageUrl도 처리하게 하거나 따로 세팅)
        profile.initialize(dto, imageUrl);

        //영속 상태이므로 명시적인 save() 호출 없이 변경 감지(Dirty Checking)로 저장됨
        log.info("[PROFILE_UPDATE_SUCCESS] Host profile updated for user: {}", user.getEmail());

    }

    public void markHostAsVerified(User user, HostProfileRequestDto dto) {
        // 1. 해당 유저의 호스트 프로필 조회
        HostProfile profile = hostProfileRepository.findByUser(user)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "호스트 프로필을 찾을 수 없습니다."));

        // 2. 인증된 정보 업데이트 (선택 사항이지만 권장)
        // 인증 시 사용한 정보를 엔티티에 저장해둡니다.
        profile.updateVerificationInfo(
            dto.getBusinessNumber(),
            dto.getRepresentativeName(),
            dto.getOpeningDate()
        );

        // 3. 인증 상태 true로 변경
        profile.markAsVerified();

        log.info("[BusinessVerify] 인증 성공 및 DB 기록 완료 - User: {}", user.getEmail());
    }
}

