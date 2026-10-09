package com.example.recruitmentbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.recruitmentbackend.entity.RecruitmentCandidate;
import com.example.recruitmentbackend.mapper.RecruitmentCandidateMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class RecruitmentService {

    private final RecruitmentCandidateMapper mapper;

    public RecruitmentService(RecruitmentCandidateMapper mapper) {
        this.mapper = mapper;
    }

    public Page<RecruitmentCandidate> list(String keyword, String passed, String hired, Long deptId,
                                           String sortField, String sortOrder, int page, int size) {
        QueryWrapper<RecruitmentCandidate> wrapper = new QueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like("candidate_name", keyword)
                    .or()
                    .like("mobile", keyword)
                    .or()
                    .like("job_name", keyword)
            );
        }
        if (passed != null && !passed.isEmpty()) {
            wrapper.eq("passed", passed);
        }
        if (hired != null && !hired.isEmpty()) {
            wrapper.eq("hired", hired);
        }
        if (deptId != null) {
            wrapper.eq("dept_id", deptId);
        }
        if (sortField != null && !sortField.isEmpty()) {
            if ("desc".equals(sortOrder)) {
                wrapper.orderByDesc(sortField);
            } else {
                wrapper.orderByAsc(sortField);
            }
        } else {
            wrapper.orderByAsc("id");
        }

        Page<RecruitmentCandidate> pageParam = new Page<>(page, size);
        return mapper.selectPage(pageParam, wrapper);
    }

    // 导出全部用
    public List<RecruitmentCandidate> listAll() {
        return mapper.selectList(null);
    }

    // 导出选中用（新加的方法）
    public List<RecruitmentCandidate> listByIds(List<Long> ids) {
        return mapper.selectBatchIds(ids);
    }

    public RecruitmentCandidate getById(Long id) {
        return mapper.selectById(id);
    }

    public RecruitmentCandidate add(RecruitmentCandidate candidate) {
        if (candidate.getMobile() == null || candidate.getMobile().isEmpty()) {
            throw new RuntimeException("手机号不能为空");
        }
        if (mapper.selectCount(new QueryWrapper<RecruitmentCandidate>()
                .eq("mobile", candidate.getMobile())) > 0) {
            throw new RuntimeException("手机号已存在");
        }
        if (candidate.getCrmSyncStatus() == null) {
            candidate.setCrmSyncStatus("未创建");
        }
        mapper.insert(candidate);
        return candidate;
    }

    public RecruitmentCandidate update(Long id, RecruitmentCandidate candidate) {
        candidate.setId(id);
        mapper.updateById(candidate);
        return mapper.selectById(id);
    }

    public void delete(Long id) {
        mapper.deleteById(id);
    }

    public void deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("未选择任何记录");
        }
        mapper.deleteBatchIds(ids);
    }

    public void clearResumePath(Long id) {
        mapper.update(null, new UpdateWrapper<RecruitmentCandidate>()
                .eq("id", id)
                .set("resume_path", null));
    }

    public void updateBatchField(Map<String, Object> body) {
        List<?> ids = (List<?>) body.get("ids");
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("未选择任何记录");
        }
        List<Long> longIds = ids.stream().map(o -> Long.valueOf(o.toString())).toList();

        UpdateWrapper<RecruitmentCandidate> wrapper = new UpdateWrapper<>();
        wrapper.in("id", longIds);

        if (body.get("passed") != null) {
            wrapper.set("passed", body.get("passed"));
        }
        if (body.get("hired") != null) {
            wrapper.set("hired", body.get("hired"));
        }
        if (body.get("deptId") != null) {
            wrapper.set("dept_id", body.get("deptId"));
            Long deptId = Long.valueOf(body.get("deptId").toString());
            for (Map<String, Object> d : depts()) {
                if (d.get("id").equals(deptId)) {
                    wrapper.set("dept_name", d.get("name"));
                    break;
                }
            }
        }

        mapper.update(null, wrapper);
    }

    public RecruitmentCandidate updateField(Long id, Map<String, Object> body) {
        RecruitmentCandidate c = mapper.selectById(id);
        if (c == null) {
            throw new RuntimeException("记录不存在");
        }

        if (c.getCrmUserId() != null) {
            throw new RuntimeException("账号已创建，不能修改");
        }

        if (body.containsKey("passed")) {
            c.setPassed((String) body.get("passed"));
        }
        if (body.containsKey("hired")) {
            c.setHired((String) body.get("hired"));
        }
        if (body.containsKey("deptId")) {
            Object deptId = body.get("deptId");
            if (deptId != null && !deptId.toString().isEmpty()) {
                c.setDeptId(Long.valueOf(deptId.toString()));
                for (Map<String, Object> d : depts()) {
                    if (d.get("id").equals(c.getDeptId())) {
                        c.setDeptName((String) d.get("name"));
                        break;
                    }
                }
            }
        }
        if (body.containsKey("jobName")) {
            c.setJobName((String) body.get("jobName"));
        }

        mapper.updateById(c);
        return c;
    }

    public RecruitmentCandidate confirmHire(Long id) {
        RecruitmentCandidate c = mapper.selectById(id);
        if (c == null) {
            throw new RuntimeException("记录不存在");
        }
        if (!"是".equals(c.getPassed())) {
            throw new RuntimeException("未通过面试，不能确认入职");
        }
        if (!"是".equals(c.getHired())) {
            throw new RuntimeException("未确认入职");
        }
        if (c.getDeptId() == null) {
            throw new RuntimeException("未选择入职部门");
        }
        if (c.getMobile() == null || c.getMobile().isEmpty()) {
            throw new RuntimeException("手机号为空");
        }
        if (c.getCrmUserId() != null) {
            throw new RuntimeException("账号已创建");
        }

        c.setCrmSyncStatus("创建中");
        mapper.updateById(c);

        try {
            Long crmUserId = callCrmCreateUser(c);
            c.setCrmUserId(crmUserId);
            c.setCrmSyncStatus("已创建");
            c.setCrmSyncTime(LocalDateTime.now());
            c.setCrmError(null);
        } catch (Exception e) {
            c.setCrmSyncStatus("失败");
            c.setCrmError(e.getMessage());
        }

        mapper.updateById(c);
        return c;
    }

    private Long callCrmCreateUser(RecruitmentCandidate c) {
        if ("13800138000".equals(c.getMobile())) {
            throw new RuntimeException("CRM 中手机号已存在");
        }
        return System.currentTimeMillis() % 100000;
    }

    public List<Map<String, Object>> depts() {
        return List.of(
                Map.of("id", 101, "name", "一线销售"),
                Map.of("id", 102, "name", "二线销售"),
                Map.of("id", 103, "name", "交付专员"),
                Map.of("id", 104, "name", "技术部"),
                Map.of("id", 105, "name", "人事部")
        );
    }

    // ================== 统计看板 ==================

    public Map<String, Object> statsOverview() {
        List<RecruitmentCandidate> all = mapper.selectList(null);
        long total = all.size();
        long passed = all.stream().filter(c -> "是".equals(c.getPassed())).count();
        long hired = all.stream().filter(c -> "是".equals(c.getHired())).count();
        long pending = all.stream().filter(c ->
                "待确认".equals(c.getPassed()) || "待确认".equals(c.getHired())).count();

        Map<String, Object> m = new java.util.HashMap<>();
        m.put("total", total);
        m.put("passed", passed);
        m.put("hired", hired);
        m.put("pending", pending);
        m.put("passRate", total == 0 ? 0 : Math.round(passed * 1000.0 / total) / 10.0);
        m.put("hireRate", total == 0 ? 0 : Math.round(hired * 1000.0 / total) / 10.0);
        return m;
    }

    public List<Map<String, Object>> statsByJob() {
        List<RecruitmentCandidate> all = mapper.selectList(null);
        Map<String, long[]> map = new java.util.LinkedHashMap<>();
        for (RecruitmentCandidate c : all) {
            String job = c.getJobName() == null ? "未填写" : c.getJobName();
            map.computeIfAbsent(job, k -> new long[3]);
            long[] arr = map.get(job);
            arr[0]++;
            if ("是".equals(c.getPassed())) arr[1]++;
            if ("是".equals(c.getHired())) arr[2]++;
        }
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Map.Entry<String, long[]> e : map.entrySet()) {
            long[] arr = e.getValue();
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("jobName", e.getKey());
            m.put("total", arr[0]);
            m.put("passed", arr[1]);
            m.put("hired", arr[2]);
            m.put("passRate", arr[0] == 0 ? 0 : Math.round(arr[1] * 1000.0 / arr[0]) / 10.0);
            result.add(m);
        }
        return result;
    }

    public List<Map<String, Object>> statsByDept() {
        List<RecruitmentCandidate> all = mapper.selectList(null);
        Map<String, long[]> map = new java.util.LinkedHashMap<>();
        for (RecruitmentCandidate c : all) {
            if (c.getDeptName() == null || c.getDeptName().isEmpty()) continue;
            map.computeIfAbsent(c.getDeptName(), k -> new long[2]);
            long[] arr = map.get(c.getDeptName());
            arr[0]++;
            if ("是".equals(c.getHired())) arr[1]++;
        }
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Map.Entry<String, long[]> e : map.entrySet()) {
            long[] arr = e.getValue();
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("deptName", e.getKey());
            m.put("total", arr[0]);
            m.put("hired", arr[1]);
            result.add(m);
        }
        return result;
    }
}