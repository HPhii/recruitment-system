package com.example.demo.document.job;

import com.example.demo.document.skill.SkillDocument;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;

import java.util.List;


@Document(indexName = "jobs_index")
@Mapping(mappingPath = "static/jobs-mapping.json") // mapping tùy chỉnh
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobDocument  {

    @Id
    @JsonProperty("job_id")
    @Field(name = "job_id")
    Long jobId;

    @JsonProperty("job_title")
    @Field(name = "job_title")
    String jobTitle;

    @JsonProperty("company_id")
    @Field(name = "company_id")
    Long companyId;

    @JsonProperty("company_name")
    @Field(name = "company_name")
    String companyName;

    @JsonProperty("salary_min")
    @Field(name = "salary_min")
    Integer salaryMin;

    @JsonProperty("salary_max")
    @Field(name = "salary_max")
    Integer salaryMax;

    @JsonProperty("skills")
    @Field(name = "skills", type = FieldType.Nested)
    List<SkillDocument> skills;
}
