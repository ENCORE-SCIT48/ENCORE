package com.encore.encore.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RelationType {
    FOLLOW,
    BLOCK, 
    PERFORMER_RECOMMEND
}
