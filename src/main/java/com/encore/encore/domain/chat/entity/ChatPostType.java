package com.encore.encore.domain.chat.entity;

import lombok.Getter;

/**
 * 공연 채팅방의 목적(유형)을 나타내는 열거형.
 * <p>
 * 기획: 공연 채팅은 "공연을 본 뒤 이어지는 공간"으로,
 * 후기·감상 공유, 택시 동승 모집, 뒤풀이(애프터) 모임 등 목적별로 구분합니다.
 * </p>
 *
 * @see ChatPost#getPostType()
 */
@Getter
public enum ChatPostType {

    /** 후기·감상 공유 (공연 본 후 이야기 나누기) */
    REVIEW("후기·감상"),

    /** 택시 동승 모집 */
    TAXI_SHARE("택시 동승"),

    /** 뒤풀이·애프터 모임 */
    AFTER_PARTY("뒤풀이"),

    /** 기타 / 일반 모집 */
    GENERAL("일반");

    private final String displayName;

    ChatPostType(String displayName) {
        this.displayName = displayName;
    }
}
