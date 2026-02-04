package com.encore.encore.domain.chat.dto;

import com.encore.encore.domain.member.entity.HostProfile;
import com.encore.encore.domain.member.entity.PerformerProfile;
import com.encore.encore.domain.member.entity.UserProfile;
import com.encore.encore.domain.performance.entity.Performance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatPostDTO {
    private Long id;
    private Performance performance;
    private HostProfile host;
    private UserProfile profile;
    private PerformerProfile performer;
    private String title;
    private String content;
    private Integer maxMember;
    private String status;
}


