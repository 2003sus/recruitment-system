package com.example.recruitmentbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("interview_schedule")
public class InterviewSchedule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long candidateId;
    private String candidateName;
    private String mobile;
    private String jobName;
    private LocalDateTime interviewTime;
    private String interviewerName;
    private String status;
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}