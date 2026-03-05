package com.encore.encore.domain.chat.dto;

import com.encore.encore.domain.chat.entity.ChatPostType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 채팅 게시글(모집글) 생성 요청 DTO.
 * <p>
 * 공연 채팅은 "공연 본 뒤 이어지는 공간"으로, 후기·감상/택시 동승/뒤풀이 등
 * 목적({@link #postType})을 선택해 모집글을 생성할 때 사용합니다.
 * </p>
 *
 * @param performanceId 게시글이 속할 공연 ID (path variable로도 전달 가능)
 * @param postType      채팅 목적. null/미전달 시 {@link ChatPostType#GENERAL}로 처리
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestCreateChatPostDto {

    private Long performanceId;
    /** 채팅 유형: REVIEW, TAXI_SHARE, AFTER_PARTY, GENERAL. 미전달 시 GENERAL */
    private String postType;
    @NotBlank(message = "제목은 필수 입니다.")
    @Size(min = 2, max = 100, message = "제목은 2자 이상 100자 이하로 입력해야 합니다.")
    private String title;
    private String content;
    @NotNull(message = "모집인원은 필수 입니다.")
    @Min(value = 2, message = "모집인원은 2명 이상으로 입력해야 합니다.")
    @Max(value = 50, message = "모집인원은 50명 이하여야 합니다.")
    private Integer maxMember;
}


