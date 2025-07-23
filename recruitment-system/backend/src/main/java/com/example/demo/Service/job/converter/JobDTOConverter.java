package com.example.demo.service.job.converter;

import com.example.demo.document.job.JobDocument;
import com.example.demo.document.skill.SkillDocument;
import com.example.demo.dto.job.JobDTO;
import com.example.demo.dto.skill.SkillDTO;
import com.example.demo.search.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
@Component
public class JobDTOConverter implements Converter<JobDocument, JobDTO> {

    @Override
    public Class<JobDocument> getDocumentClass() {
        return JobDocument.class;
    }

    @Override
    public JobDTO convertToDto(JobDocument doc) {
        return JobDTO.builder()
                .jobId(doc.getJobId())
                .jobTitle(doc.getJobTitle())
                .companyId(doc.getCompanyId())
                .companyName(doc.getCompanyName())
                .salaryMin(doc.getSalaryMin())
                .salaryMax(doc.getSalaryMax())
                .skills(doc.getSkills() != null ? doc.getSkills().stream()
                        .map(s -> SkillDTO
                                .builder()
                                .skillId(s.getSkillId())
                                .skillName(s.getSkillName()).build()).toList() : null)
                .build();
    }
    @Override
    public JobDocument convertToDocument(JobDTO dto) {
        return JobDocument.builder()
                .jobId(dto.getJobId())
                .jobTitle(dto.getJobTitle())
                .companyId(dto.getCompanyId())
                .companyName(dto.getCompanyName())
                .salaryMin(dto.getSalaryMin())
                .salaryMax(dto.getSalaryMax())
//                .skills(dto.getSkills().stream()
//                        .map(s -> SkillDocument.builder().skillName(s.getSkillName()).build()).toList() : null)
//                        .collect(Collectors.toList()))
//                .build();
                .skills(dto.getSkills() != null ? dto.getSkills().stream()
                        .map(s -> SkillDocument.builder().skillName(s.getSkillName()).build()).toList() : null)
                .build();
    }
}
