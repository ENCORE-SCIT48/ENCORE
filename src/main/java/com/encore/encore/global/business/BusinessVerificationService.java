package com.encore.encore.global.business;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessVerificationService {

    // 설정 파일에서 값을 읽어옴
    @Value("${nts.api.key}")
    private String apiKey;

    @Value("${nts.api.url}")
    private String baseUrl;

    public boolean verifyBusiness(String bNo, String startDt, String pNm) {
        RestTemplate restTemplate = new RestTemplate();

        // 1. 요청 바디 생성
        NtsValidateRequest request = NtsValidateRequest.builder()
            .businesses(List.of(NtsValidateRequest.BusinessInfo.builder()
                .b_no(bNo)
                .start_dt(startDt)
                .p_nm(pNm)
                .build()))
            .build();

        // 2. 헤더 및 URL 설정 (Query 방식 사용)
        String url = baseUrl + "?serviceKey=" + apiKey;

        try {
            NtsValidateResponse response = restTemplate.postForObject(url, request, NtsValidateResponse.class);

            if (response != null && response.getData() != null && !response.getData().isEmpty()) {
                String isValid = response.getData().get(0).getValid();
                return "01".equals(isValid); // 01이면 정상 사업자
            }
        } catch (Exception e) {
            log.error("국세청 API 호출 중 오류 발생: {}", e.getMessage());
        }
        return false;
    }
}
