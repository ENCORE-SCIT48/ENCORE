package com.encore.encore.global.business;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NtsValidateRequest {

    private List<BusinessInfo> businesses;

    @Getter
    @Builder
    public static class BusinessInfo {
        private String b_no;        // 사업자번호 (10자리)
        private String start_dt;    // 개업일자 (YYYYMMDD)
        private String p_nm;        // 대표자성명
    }
}
