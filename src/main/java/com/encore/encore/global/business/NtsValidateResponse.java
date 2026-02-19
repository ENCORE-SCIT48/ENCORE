package com.encore.encore.global.business;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class NtsValidateResponse {
    private Integer request_cnt;
    private Integer valid_cnt;
    private List<NtsResult> data;

    @Getter
    @NoArgsConstructor
    public static class NtsResult {
        private String b_no;
        private String valid; // "01"이면 성공, "02"면 실패
        private String status; // 상세 에러 메시지 등
    }
}
