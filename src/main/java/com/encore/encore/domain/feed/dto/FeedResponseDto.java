package com.encore.encore.domain.feed.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FeedResponseDto {
    private List<FeedItemDto> items;
}
