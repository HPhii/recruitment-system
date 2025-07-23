package com.example.demo.Service;

import com.example.demo.entity.JobDemand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ForecastService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<JobDemand> forecastItDemand() {
        String sql = """
            SELECT DATE_TRUNC('month', j.approved_on) AS month,
                   COUNT(*) AS job_count
            FROM jobs j
            JOIN job_industries_v3 ji ON j.job_id = ji.job_id
            JOIN industries_v3 i ON ji.industry_v3_id = i.industry_v3_id
            WHERE i.industry_v3_name ILIKE '%IT%' AND j.approved_on >= date_trunc('year', CURRENT_DATE)
            GROUP BY month
            ORDER BY month
        """;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        List<JobDemand> history = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            YearMonth month = YearMonth.parse(row.get("month").toString().substring(0, 7));
            Long count = ((Number) row.get("job_count")).longValue();
            history.add(new JobDemand(month, count, false));
        }

        // Tính trung bình tăng trưởng
        List<Long> counts = history.stream().map(JobDemand::getJobCount).collect(Collectors.toList());
        double avgGrowth = 0;
        if (counts.size() >= 2) {
            long diffSum = 0;
            for (int i = 1; i < counts.size(); i++) {
                diffSum += counts.get(i) - counts.get(i - 1);
            }
            avgGrowth = (double) diffSum / (counts.size() - 1);
        }

        // Tạo dữ liệu dự đoán cho 4 tháng cuối năm
        YearMonth lastMonth = history.isEmpty() ? YearMonth.now() : history.get(history.size() - 1).getMonth();
        Long lastCount = history.isEmpty() ? 0 : history.get(history.size() - 1).getJobCount();

        for (int i = 1; i <= 4; i++) {
            lastMonth = lastMonth.plusMonths(1);
            lastCount = Math.max(0, Math.round(lastCount + avgGrowth));
            history.add(new JobDemand(lastMonth, lastCount, true));
        }

        return history;
    }
}
