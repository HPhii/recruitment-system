package com.example.demo.entity;

import java.time.YearMonth;


public class JobDemand {
     YearMonth month;
     Long jobCount;
     boolean isForecast;

    public JobDemand(YearMonth month, Long jobCount, boolean isForecast) {
        this.month = month;
        this.jobCount = jobCount;
        this.isForecast = isForecast;
    }

    public YearMonth getMonth() {
        return month;
    }

    public void setMonth(YearMonth month) {
        this.month = month;
    }

    public Long getJobCount() {
        return jobCount;
    }

    public void setJobCount(Long jobCount) {
        this.jobCount = jobCount;
    }

    public boolean isForecast() {
        return isForecast;
    }

    public void setForecast(boolean forecast) {
        isForecast = forecast;
    }
}
