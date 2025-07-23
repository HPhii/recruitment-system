package com.example.demo.dto.job;

import com.example.demo.dto.skill.SkillDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobDTO {
    Long jobId;
    String jobTitle;
    Long companyId;
    String companyName;
    Integer salaryMin;
    Integer salaryMax;
    List<SkillDTO> skills;
}