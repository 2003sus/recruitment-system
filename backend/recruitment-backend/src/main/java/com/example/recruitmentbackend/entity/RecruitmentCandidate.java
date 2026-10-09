package com.example.recruitmentbackend.entity;


import cn.idev.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("recruitment_candidate")
public class RecruitmentCandidate {

    @TableId(type = IdType.AUTO)
    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty("岗位")
    private String jobName;

    @ExcelProperty("姓名")
    private String candidateName;

    @ExcelProperty("手机号")
    private String mobile;

    @ExcelProperty("面试负责人")
    private String interviewerName;

    @ExcelProperty("是否通过")
    private String passed;

    @ExcelProperty("是否入职")
    private String hired;

    @ExcelProperty("入职部门")
    private String deptName;

    private Long deptId;
    private String resumePath;
    private String crmSyncStatus;
    private Long crmUserId;
    private String crmError;
    private LocalDateTime crmSyncTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}