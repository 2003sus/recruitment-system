package com.example.recruitmentbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.recruitmentbackend.entity.RecruitmentCandidate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RecruitmentCandidateMapper extends BaseMapper<RecruitmentCandidate> {
}