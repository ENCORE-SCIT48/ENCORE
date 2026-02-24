package com.encore.encore.global.business;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class NtsStatusResponse {
    private String status_code;
    private List<StatusData> data;

    @Getter @NoArgsConstructor
    public static class StatusData {
        private String b_no;
        private String b_stt;    // "계속사업자", "휴업자", "폐업자" 등
        private String b_stt_cd; // "01", "02", "03" 등
        private String tax_type; // 과세유형
    }
}
