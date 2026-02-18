package com.encore.encore.domain.member.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileRequestDto {
    private String phoneNumber;
    private LocalDate birthDate;
    private String location;
    private String introduction;
    private List<String> preferredGenres;
    private List<String> preferredPerformanceTypes;
 }
