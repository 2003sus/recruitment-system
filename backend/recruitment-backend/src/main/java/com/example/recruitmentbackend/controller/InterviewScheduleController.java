package com.example.recruitmentbackend.controller;

import com.example.recruitmentbackend.entity.InterviewSchedule;
import com.example.recruitmentbackend.service.InterviewScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/interview")
public class InterviewScheduleController {

    private final InterviewScheduleService service;

    public InterviewScheduleController(InterviewScheduleService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public List<InterviewSchedule> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return service.listByRange(start, end);
    }

    @GetMapping("/{id}")
    public InterviewSchedule getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public InterviewSchedule add(@RequestBody InterviewSchedule s) {
        return service.add(s);
    }

    @PutMapping("/{id}")
    public InterviewSchedule update(@PathVariable Long id, @RequestBody InterviewSchedule s) {
        return service.update(id, s);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}