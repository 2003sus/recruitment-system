package com.example.recruitmentbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("hr_user")
public class HrUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
}