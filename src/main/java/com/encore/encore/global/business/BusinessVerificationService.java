package com.encore.encore.global.business;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * [설명] 국세청 API를 연동하여 사업자 등록 상태를 검증하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessVerificationService {

    @Value("${nts.api.key}")
    private String apiKey;

    @Value("${nts.api.url.status}")
    private String statusUrl;

    /**
     * [설명] 사업자 등록 번호를 기반으로 현재 휴·폐업 상태 및 과세 유형을 조회합니다.
     *
     * @param bNo 사업자 등록 번호 (하이픈 포함 가능)
     * @return 국세청 API 응답 데이터 중 첫 번째 상태 정보 (NtsStatusResponse.StatusData)
     */
    public NtsStatusResponse.StatusData getBusinessStatusData(String bNo) {
        log.info("[BusinessVerificationService] 사업자 상태 조회 시작 - 입력번호: {}", bNo);

        RestTemplate restTemplate = new RestTemplate();

        // 구현 주석: API 규격에 맞추기 위해 입력된 하이픈(-)을 모두 제거함
        String cleanBNo = bNo.replaceAll("-", "");

        NtsStatusRequest request = NtsStatusRequest.builder()
            .b_no(List.of(cleanBNo))
            .build();

        // 구현 주석: 국세청 API 요청 URL 및 인증키 설정
        String url = statusUrl + "?serviceKey=" + apiKey;

        try {
            NtsStatusResponse response = restTemplate.postForObject(url, request, NtsStatusResponse.class);

            if (response != null && response.getData() != null && !response.getData().isEmpty()) {
                NtsStatusResponse.StatusData result = response.getData().get(0);

                // INFO: 조회 성공 결과 로그 기록 (사업자 상태 포함)
                log.info("[BusinessVerificationService] 조회 성공 - 번호: {}, 상태: {}"
                    , cleanBNo, result.getB_stt());
                return result; // 조회된 첫 번째 데이터 반환
            }
            log.warn("[BusinessVerificationService] API 응답은 성공했으나 데이터가 비어있음 - 번호: {}", cleanBNo);
        } catch (Exception e) {
            // ERROR: 통신 장애 및 API 예외 상황 발생 시 Stack Trace 포함 기록
            log.error("[BusinessVerificationService] 국세청 API 호출 중 예외 발생 - 번호: {}, 사유: {}"
                , cleanBNo, e.getMessage(), e);
        }
        return null;
    }
}
