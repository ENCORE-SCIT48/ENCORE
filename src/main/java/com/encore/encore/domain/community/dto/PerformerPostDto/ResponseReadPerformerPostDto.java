package com.encore.encore.domain.community.dto.PerformerPostDto;


import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseReadPerformerPostDto {

    private Long postId;

    private String postType;

    private String title;

    private String content;

    private Integer capacity;

    private Integer approvedCount;

    private Integer viewCount;

    private LocalDateTime createdAt;

    private Long performerId;

}
