package com.encore.encore.domain.community.dto;

import java.util.List;

import com.encore.encore.domain.community.entity.Post;
import com.encore.encore.domain.community.entity.PostInteraction;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceManageDto {
    
    private Post post;
    private List<PostInteraction> applicants;

}
