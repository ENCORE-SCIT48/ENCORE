package com.encore.encore.domain.member.service;

import com.encore.encore.domain.member.dto.UserProfileRequestDto;
import com.encore.encore.domain.member.dto.UserProfileResponseDto;
import com.encore.encore.domain.member.entity.UserProfile;
import com.encore.encore.domain.member.repository.UserProfileRepository;
import com.encore.encore.global.common.service.FileService;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final FileService fileService;

    /**
     * 사용자의 이메일(username)를 이용해 프로필 정보를 조회합니다.
     *
     * @param username 유저 식별자 (ID)
     * @return 조회된 유저 프로필 Dto
     * @throws ApiException 유저 정보가 없을 경우 예외 발생
     */
    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserProfile(String username) {
        log.info("[ProfileService] 프로필 조회 시도 - Email: {}", username);

        UserProfile userProfile = userProfileRepository.findByUser_Email(username)
            .orElseThrow(() -> {
                log.warn("[ProfileService] 프로필 조회 실패 - 해당 유저의 프로필 데이터가 DB에 존재하지 않음." +
                    " Username: {}", username);
                return new ApiException(ErrorCode.NOT_FOUND, "해당 이메일의 유저 프로필이 존재하지 않습니다.");
            });
        log.info("[ProfileService] 프로필 조회 성공 - Username: {}, ProfileID: {}",
            username, userProfile.getProfileId());

        return UserProfileResponseDto.from(userProfile);
    }

    /**
     * [설명] 사용자의 프로필 정보를 업데이트하고 초기 설정을 완료합니다.
     *
     * @param username     현재 로그인한 사용자의 식별자 (Email)
     * @param dto          수정할 프로필 정보가 담긴 요청 객체
     * @param profileImage 업로드할 프로필 이미지 파일 (선택 사항)
     * @throws ApiException 해당 유저를 찾을 수 없을 때 NOT_FOUND 에러 발생
     */
    @Transactional
    public void updateProfile(String username,
                              UserProfileRequestDto dto,
                              MultipartFile profileImage) {
        log.info("[PROFILE_UPDATE_START] User: {}", username);
        //데이터 유무 체크
        UserProfile userProfile = userProfileRepository.findByUser_Email(username).orElseThrow(() -> {
            log.error("[PROFILE_UPDATE_ERROR] Profile not found for user: {}", username);
            return new ApiException(ErrorCode.NOT_FOUND);
        });
        // 2. 이미지 파일이 있으면 업로드 후 URL 반환, 없으면 null
        String imageUrl = null;

        if (profileImage != null && !profileImage.isEmpty()) {
            // [추가] 기존에 등록된 이미지가 있다면 저장소에서 삭제
            if (userProfile.getProfileImageUrl() != null) {
                log.info("[FILE_DELETE] Deleting old profile image: {}", userProfile.getProfileImageUrl());
                fileService.deletePhysicalFile(userProfile.getProfileImageUrl());
            }

            // 새 파일 업로드
            imageUrl = fileService.saveFile(profileImage);

            log.info("[FILE_UPLOAD] New profile image uploaded: {}", imageUrl);
        }

        userProfile.initialize(dto, imageUrl);

        log.info("[PROFILE_UPDATE_SUCCESS] Profile updated for user: {}", username);
    }
}
