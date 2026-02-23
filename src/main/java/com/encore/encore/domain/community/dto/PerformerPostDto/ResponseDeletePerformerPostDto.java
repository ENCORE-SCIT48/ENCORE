package com.encore.encore.domain.community.dto.PerformerPostDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseDeletePerformerPostDto {

    private Long postId;

    private boolean deleted;
}
