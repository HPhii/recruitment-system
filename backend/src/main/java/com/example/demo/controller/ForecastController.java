package com.example.demo.controller;

import com.example.demo.Service.ForecastService;
import com.example.demo.entity.JobDemand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/forecast")
public class ForecastController {

    @Autowired
    private ForecastService forecastService;

    @GetMapping("/it-demand")
    public List<JobDemand> getItForecast() {
        return forecastService.forecastItDemand();
    }
}
