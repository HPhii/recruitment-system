package com.example.demo.service.job.converter;

import com.example.demo.document.job.JobDocument;
import com.example.demo.dto.job.JobDTO;
import com.example.demo.repository.CvRepository;
import com.example.demo.search.ElasticsearchProxy;
import com.example.demo.search.SearchFilters;
import com.example.demo.search.query.QueryType;
import com.example.demo.search.query.SearchMeta;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobService {
    CvRepository cvRepository;
    JobDTOConverter jobDTOConverter;
    ElasticsearchProxy<JobDocument, JobDTO> client;
    public List<JobDTO> searchJobsBySkillPrefix (SearchFilters filters){
        log.info("Searching jobs with term: {}", filters.getTerm());
        return client.search(
                filters,
                new SearchMeta(List.of("skills.skill_name"),"jobs_index", QueryType.NESTED_PREFIX),
                JobDocument.class
        );
    }
}
