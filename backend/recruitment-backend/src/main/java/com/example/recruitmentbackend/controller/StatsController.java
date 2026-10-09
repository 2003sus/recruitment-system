package com.example.recruitmentbackend.controller;

import com.example.recruitmentbackend.service.RecruitmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recruitment/stats")
public class StatsController {

    private final RecruitmentService service;

    public StatsController(RecruitmentService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    public Map<String, Object> overview() {
        return service.statsOverview();
    }

    @GetMapping("/by-job")
    public List<Map<String, Object>> byJob() {
        return service.statsByJob();
    }

    @GetMapping("/by-dept")
    public List<Map<String, Object>> byDept() {
        return service.statsByDept();
    }
}