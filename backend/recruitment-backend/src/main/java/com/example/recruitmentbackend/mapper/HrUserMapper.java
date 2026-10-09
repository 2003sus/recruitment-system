package com.example.recruitmentbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.recruitmentbackend.entity.HrUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HrUserMapper extends BaseMapper<HrUser> {
}