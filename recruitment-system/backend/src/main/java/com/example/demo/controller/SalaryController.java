package com.example.demo.controller;

import com.example.demo.Service.SalaryService;
import com.example.demo.entity.IndustrySalary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/salary")
public class SalaryController {
    @Autowired
    private SalaryService salaryService;

    @GetMapping("/average-by-industry")
    public List<IndustrySalary> getAverageSalaryByIndustry() {
        return salaryService.getAverageSalaryByIndustry();
    }
}
