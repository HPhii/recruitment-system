package com.example.demo.entity;

public class IndustrySalary {
    private String industryName;
    private Double averageSalary;

    public IndustrySalary(String industryName, Double averageSalary) {
        this.industryName = industryName;
        this.averageSalary = averageSalary;
    }

    public String getIndustryName() {
        return industryName;
    }

    public void setIndustryName(String industryName) {
        this.industryName = industryName;
    }

    public Double getAverageSalary() {
        return averageSalary;
    }

    public void setAverageSalary(Double averageSalary) {
        this.averageSalary = averageSalary;
    }
}
