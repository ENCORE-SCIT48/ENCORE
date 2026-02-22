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

    @Value("${nts.api.key}")
    private String apiKey;

    @Value("${nts.api.url.status}")
    private String statusUrl;

    // 1. 단순 정보 조회용 (화면에 보여줄 데이터 반환)
    public NtsStatusResponse.StatusData getBusinessStatusData(String bNo) {
        RestTemplate restTemplate = new RestTemplate();
        String cleanBNo = bNo.replaceAll("-", "");

        NtsStatusRequest request = NtsStatusRequest.builder()
            .b_no(List.of(cleanBNo))
            .build();

        String url = statusUrl + "?serviceKey=" + apiKey;

        try {
            NtsStatusResponse response = restTemplate.postForObject(url, request, NtsStatusResponse.class);

            if (response != null && response.getData() != null && !response.getData().isEmpty()) {
                return response.getData().get(0); // 조회된 첫 번째 데이터 반환
            }
        } catch (Exception e) {
            log.error("API 호출 중 오류 발생: {}", e.getMessage());
        }
        return null;
    }
}
