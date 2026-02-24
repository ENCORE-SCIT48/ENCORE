package com.encore.encore.global.business;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NtsStatusRequest {
    private List<String> b_no; // 상태조회는 번호 리스트만 보냄
}
