package com.example.demo.Service;

import com.example.demo.entity.IndustrySalary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalaryService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<IndustrySalary> getAverageSalaryByIndustry() {
        String sql = """
            SELECT i.industry_v3_name, 
                   ROUND(AVG((j.salary_min + j.salary_max)/2), 0) AS avg_salary
            FROM jobs j
            JOIN job_industries_v3 ji ON j.job_id = ji.job_id
            JOIN industries_v3 i ON ji.industry_v3_id = i.industry_v3_id
            WHERE j.salary_min IS NOT NULL AND j.salary_max IS NOT NULL
            GROUP BY i.industry_v3_name
            ORDER BY avg_salary DESC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new IndustrySalary(
                rs.getString("industry_v3_name"),
                rs.getDouble("avg_salary")
        ));
    }
}
