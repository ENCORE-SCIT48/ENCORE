package com.encore.encore.global.common.service;


import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * [공통] 파일 업로드 및 삭제 처리를 담당하는 서비스 클래스입니다.
 */
@Service
@Slf4j
public class FileService {

    /**
     * application.yaml에 설정된 파일 저장 물리 경로
     */
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 클라이언트로부터 받은 파일을 물리적 저장소에 저장하고, DB에 저장할 웹 경로를 반환합니다.
     *
     * @param file 업로드할 멀티파트 파일 객체
     * @return 저장된 파일의 웹 접근 경로 (예: /uploads/uuid.jpg), 파일이 없을 경우 null 반환
     * @throws ApiException 파일 저장 중 IOException 발생 시 FILE_UPLOAD_ERROR 던짐
     */
    public String saveFile(MultipartFile file) {
        // 1. 안전장치: 파일이 없으면 로직 실행 안 함
        if (file == null || file.isEmpty()) {
            log.info("[FileService] 업로드 요청된 파일이 비어있습니다.");
            return null;
        }
        // 2. 폴더 자동 생성: C:/encore/uploads/ 폴더가 없으면 에러 나니까 코드로 만듦
        File folder = new File(uploadDir);
        if (!folder.exists()) {
            boolean isCreated = folder.mkdirs();
            log.info("[FileService] 업로드 디렉토리 생성 결과: {}, 경로: {}", isCreated, uploadDir);
        }

        // 3. 파일명 중복 해결:
        // 유저 A와 B가 둘 다 'profile.jpg'를 올리면 덮어씌워지는 걸 방지하기 위해
        // UUID(무작위 식별자)를 붙여서 이름을 바꿉니다. (예: 550e8400-e29b...jpg)
        String originalFileName = file.getOriginalFilename();
        String extension = ""; // 기본값은 빈 문자열

        // 파일명이 존재하고, 마침표를 포함하고 있을 때만 잘라내기
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String savedFileName = UUID.randomUUID().toString() + extension;
        String fullPath = uploadDir + savedFileName;

        // 4. 물리적 저장: 설정한 폴더 안에 새 이름으로 파일을 저장합니다.
        try {
            log.info("[FileService] 파일 저장 시도: {} -> {}", originalFileName, savedFileName);
            file.transferTo(new File(fullPath));
            log.info("[FileService] 파일 저장 성공: {}", fullPath);
        } catch (IOException e) {
            log.error("[FileService] 파일 물리 저장 중 예외 발생. 원인: {}", e.getMessage(), e);
            throw new ApiException(ErrorCode.FILE_UPLOAD_ERROR);
        }

        // 5. 결과 반환: DB에 저장될 "웹용 주소"를 돌려줍니다.
        // 실제 경로는 C:/... 지만, 브라우저용 주소는 "/uploads/파일명" 이어야 합니다.
        return "/uploads/" + savedFileName;
    }

    /**
     * 서버 하드디스크에 저장된 파일을 물리적으로 삭제합니다.
     * 삭제 중 발생하는 예외는 서비스 흐름에 영향을 주지 않도록 내부에서 처리(Log)합니다.
     *
     * @param filePath 삭제할 파일의 웹 접근 경로 (예: /uploads/uuid.jpg)
     */
    public void deletePhysicalFile(String filePath) {
        try {
            if (filePath == null) return;

            String fileName = filePath.replace("/uploads/", "");
            File file = new File(uploadDir + fileName);

            if (file.exists()) {
                file.delete(); // 여기서 에러가 나더라도...
            }
        } catch (Exception e) {
            // 에러를 'throw' 하지 않고 'log'만 남기면 서비스는 멈추지 않고 계속 흘러갑니다.
            log.error("파일 삭제 중 문제가 발생했지만 무시하고 진행합니다: {}", e.getMessage());
        }
    }
}
