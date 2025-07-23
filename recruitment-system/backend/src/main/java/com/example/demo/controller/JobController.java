package com.example.demo.controller;

import com.example.demo.dto.job.JobDTO;
import com.example.demo.search.SearchFilters;
import com.example.demo.service.job.converter.JobService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobController {
    JobService jobService;
    @PostMapping("/search")
    public List<JobDTO> search(@RequestBody SearchFilters filters) {
        return jobService.searchJobsBySkillPrefix(filters);
    }

}
