package com.encore.encore.domain.member.service;

import com.encore.encore.domain.member.dto.HostProfileRequestDto;
import com.encore.encore.domain.member.dto.HostProfileResponseDto;
import com.encore.encore.domain.member.entity.HostProfile;
import com.encore.encore.domain.member.repository.HostProfileRepository;
import com.encore.encore.domain.user.entity.User;
import com.encore.encore.global.common.service.FileService;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import jakarta.transaction.Transactional;
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

    /**
     * [설명] 특정 유저의 호스트 프로필 정보를 조회. 없으면 null 반환 (프로필 선택 페이지 등에서 사용)
     */
    public HostProfileResponseDto getHostProfileOrNull(User user) {
        return hostProfileRepository.findByUser(user)
            .map(HostProfileResponseDto::from)
            .orElse(null);
    }

    /**
     * [설명] 특정 유저의 호스트 프로필 정보를 조회하여 반환합니다.
     * @param user 조회 대상 유저 엔티티
     * @return 호스트 프로필 정보 응답 DTO
     */
    public HostProfileResponseDto getHostProfile(User user) {
        log.info("[HostProfileService] 프로필 데이터 조회 시작 - User: {}", user.getEmail());

        HostProfile profile = hostProfileRepository.findByUser(user)
            .orElseThrow(() -> {
                log.error("[HostProfileService] 프로필 조회 실패 - 해당 유저의 프로필이 존재하지 않음: {}", user.getEmail());
                return new ApiException(ErrorCode.NOT_FOUND, "해당 유저의 호스트 프로필을 찾을 수 없습니다.");
            });

        return HostProfileResponseDto.from(profile);
    }

    /**
     * [설명] 호스트 프로필 정보를 업데이트합니다. 이미지 변경 시 기존 파일은 삭제됩니다.
     * @param user 수정 주체 유저 엔티티
     * @param dto 수정할 데이터 객체
     * @param profileImage 새 프로필 이미지 파일 (Nullable)
     */
    @Transactional
    public void updateHostProfile(User user,
                                  @Valid HostProfileRequestDto dto,
                                  MultipartFile profileImage) {

        log.info("[HostProfileService] 프로필 업데이트 시작 - User: {}", user.getEmail());

        HostProfile profile = hostProfileRepository.findByUser(user)
            .orElseThrow(() -> {
                log.warn("[HostProfileService] 퍼포머 프로필 조회 실패 - User : {}", user);
                return new ApiException(ErrorCode.NOT_FOUND, "등록된 호스트 프로필이 없습니다.");
            });

        // 사업자 인증(Verified)이 선행되지 않은 경우 데이터 수정을 금지함
        if (!profile.isVerified()) {
            log.warn("[HostProfileService] 인증 미완료 상태에서의 접근 차단 - User: {}", user.getEmail());
            throw new ApiException(ErrorCode.UNAUTHORIZED, "사업자 인증을 먼저 완료해주세요.");
        }

        String imageUrl = profile.getProfileImageUrl(); // 기존 URL을 기본값으로 유지

        // 새로운 이미지가 전송된 경우 기존 파일을 삭제하고 새 파일을 저장함
        if (profileImage != null && !profileImage.isEmpty()) {
            if (profile.getProfileImageUrl() != null) {
                log.info("[HostProfileService] Deleting old Host image: {}", profile.getProfileImageUrl());
                fileService.deletePhysicalFile(profile.getProfileImageUrl());
            }

            imageUrl = fileService.saveFile(profileImage);
            log.info("[HostProfileService] 신규 이미지 업로드 성공: {}", imageUrl);
        } else {
            log.info("[HostProfileService] 기존 프로필 이미지 유지 - User: {}", user.getEmail());
        }

        // 데이터 업데이트
        profile.initialize(dto, imageUrl);

        //영속 상태이므로 명시적인 save() 호출 없이 변경 감지(Dirty Checking)로 저장됨
        log.info("[PROFILE_UPDATE_SUCCESS] Host profile updated for user: {}", user.getEmail());

    }

    /**
     * [설명] 사업자 번호 인증 성공 시 인증 상태를 활성화하고 번호를 저장합니다.
     * @param user 인증을 수행한 유저 엔티티
     * @param bNo 인증된 사업자 번호 (하이픈 포함 가능)
     */
    @Transactional
    public void markHostAsVerifiedOnly(User user, String bNo) {
        log.info("[HostProfileService] 인증 상태 변경 프로세스 시작 - User: {}, BizNum: {}", user.getEmail(), bNo);

        HostProfile profile = hostProfileRepository.findByUser(user)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "프로필을 찾을 수 없습니다."));

        // 인증 상태 변경
        profile.markAsVerified();


        if (bNo != null) {
            // 하이픈을 제거하지 않고 원본(000-00-00000) 그대로 저장
            profile.updateVerificationInfo(bNo, null, null);
            log.info("[VERIFY_SUCCESS] 사업자 번호 저장(하이픈 포함): {}", bNo);
        }
    }
}

