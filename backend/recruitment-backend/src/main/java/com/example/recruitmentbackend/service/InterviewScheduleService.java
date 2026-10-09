package com.example.recruitmentbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.recruitmentbackend.entity.InterviewSchedule;
import com.example.recruitmentbackend.mapper.InterviewScheduleMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class InterviewScheduleService {

    private final InterviewScheduleMapper mapper;

    public InterviewScheduleService(InterviewScheduleMapper mapper) {
        this.mapper = mapper;
    }

    /** 按日期范围查（用于日历） */
    public List<InterviewSchedule> listByRange(LocalDate start, LocalDate end) {
        LocalDateTime s = start.atStartOfDay();
        LocalDateTime e = end.atTime(LocalTime.MAX);
        QueryWrapper<InterviewSchedule> w = new QueryWrapper<>();
        w.ge("interview_time", s).le("interview_time", e);
        w.orderByAsc("interview_time");
        return mapper.selectList(w);
    }

    public InterviewSchedule getById(Long id) {
        return mapper.selectById(id);
    }

    public InterviewSchedule add(InterviewSchedule s) {
        if (s.getCandidateName() == null || s.getCandidateName().isEmpty()) {
            throw new RuntimeException("候选人姓名不能为空");
        }
        if (s.getInterviewTime() == null) {
            throw new RuntimeException("面试时间不能为空");
        }
        if (s.getStatus() == null) {
            s.setStatus("待面试");
        }
        mapper.insert(s);
        return s;
    }

    public InterviewSchedule update(Long id, InterviewSchedule s) {
        InterviewSchedule old = mapper.selectById(id);
        if (old == null) {
            throw new RuntimeException("排期不存在");
        }
        s.setId(id);
        mapper.updateById(s);
        return mapper.selectById(id);
    }

    public void delete(Long id) {
        mapper.deleteById(id);
    }
}