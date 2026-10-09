package com.example.recruitmentbackend.controller;

import cn.idev.excel.FastExcel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.recruitmentbackend.entity.RecruitmentCandidate;
import com.example.recruitmentbackend.service.RecruitmentService;
import com.example.recruitmentbackend.util.WordToPdfUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/recruitment")
public class RecruitmentController {

    private final RecruitmentService service;

    @Value("${upload.dir}")
    private String uploadDir;

    public RecruitmentController(RecruitmentService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public Page<RecruitmentCandidate> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String passed,
            @RequestParam(required = false) String hired,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.list(keyword, passed, hired, deptId, sortField, sortOrder, page, size);
    }

    @GetMapping("/depts")
    public List<Map<String, Object>> depts() {
        return service.depts();
    }

    @GetMapping("/jobs")
    public List<String> jobs() {
        return List.of("一线销售", "二线销售", "交付专员", "技术开发", "产品经理");
    }

    @GetMapping("/{id}")
    public RecruitmentCandidate getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public RecruitmentCandidate add(@RequestBody RecruitmentCandidate candidate) {
        return service.add(candidate);
    }

    @PutMapping("/{id}")
    public RecruitmentCandidate update(@PathVariable Long id, @RequestBody RecruitmentCandidate candidate) {
        return service.update(id, candidate);
    }

    @PutMapping("/{id}/field")
    public RecruitmentCandidate updateField(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return service.updateField(id, body);
    }

    @PutMapping("/batch/field")
    public void updateBatchField(@RequestBody Map<String, Object> body) {
        service.updateBatchField(body);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @DeleteMapping("/batch/delete")
    public void deleteBatch(@RequestBody List<Long> ids) {
        service.deleteBatch(ids);
    }

    @DeleteMapping("/{id}/resume-path")
    public void clearResumePath(@PathVariable Long id) {
        service.clearResumePath(id);
    }

    @PostMapping("/{id}/confirm-hire")
    public RecruitmentCandidate confirmHire(@PathVariable Long id) {
        return service.confirmHire(id);
    }

    @PostMapping("/upload")
    public Map<String, String> upload(@RequestParam("file") MultipartFile file) {
        Map<String, String> result = new HashMap<>();

        if (file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new RuntimeException("文件名无效");
        }

        String lower = originalName.toLowerCase();
        if (!lower.endsWith(".pdf") && !lower.endsWith(".doc") && !lower.endsWith(".docx")
                && !lower.endsWith(".jpg") && !lower.endsWith(".jpeg")
                && !lower.endsWith(".png") && !lower.endsWith(".gif")) {
            throw new RuntimeException("只支持 PDF、DOC、DOCX、JPG、PNG、GIF");
        }

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String ext = originalName.substring(originalName.lastIndexOf("."));
        String newName = UUID.randomUUID().toString() + ext;
        File savedFile = new File(dir, newName);

        try {
            file.transferTo(savedFile);
        } catch (IOException e) {
            throw new RuntimeException("保存失败: " + e.getMessage());
        }

        String finalPath = savedFile.getAbsolutePath().replace("\\", "/");

        if (lower.endsWith(".doc") || lower.endsWith(".docx")) {
            try {
                String pdfName = newName.replaceAll("\\.(doc|docx)$", ".pdf");
                WordToPdfUtil.convert(savedFile.getAbsolutePath(), uploadDir);
                finalPath = (uploadDir + "/" + pdfName).replace("\\", "/");
            } catch (Exception e) {
                throw new RuntimeException("Word 转 PDF 失败: " + e.getMessage());
            }
        }

        result.put("path", finalPath);
        result.put("originalName", originalName);
        return result;
    }

    @DeleteMapping("/resume")
    public void deleteResume(@RequestParam String path) {
        File pdfFile = new File(path);
        if (pdfFile.exists()) {
            pdfFile.delete();
        }
        if (path.toLowerCase().endsWith(".pdf")) {
            String base = path.substring(0, path.lastIndexOf("."));
            for (String ext : new String[]{".doc", ".docx"}) {
                File f = new File(base + ext);
                if (f.exists()) {
                    f.delete();
                }
            }
        }
    }

    @GetMapping("/download")
    public void download(@RequestParam String path, HttpServletResponse response) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException("文件不存在");
        }

        String lower = file.getName().toLowerCase();
        if (lower.endsWith(".pdf")) {
            response.setContentType("application/pdf");
        } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            response.setContentType("image/jpeg");
        } else if (lower.endsWith(".png")) {
            response.setContentType("image/png");
        } else if (lower.endsWith(".gif")) {
            response.setContentType("image/gif");
        } else {
            response.setContentType("application/octet-stream");
        }

        response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
        Files.copy(file.toPath(), response.getOutputStream());
        response.getOutputStream().flush();
    }

    @PostMapping("/export")
    public void export(@RequestBody(required = false) List<Long> ids, HttpServletResponse response) throws IOException {
        List<RecruitmentCandidate> data;
        if (ids != null && !ids.isEmpty()) {
            data = service.listByIds(ids);
        } else {
            data = service.listAll();
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("招聘记录", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        FastExcel.write(response.getOutputStream(), RecruitmentCandidate.class)
                .sheet("招聘记录")
                .doWrite(data);
    }

    @PostMapping("/import")
    public Map<String, Object> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        List<RecruitmentCandidate> list = FastExcel.read(file.getInputStream())
                .head(RecruitmentCandidate.class)
                .sheet()
                .doReadSync();

        int success = 0;
        int fail = 0;
        for (RecruitmentCandidate c : list) {
            try {
                if (c.getMobile() == null || c.getMobile().isEmpty()) {
                    fail++;
                    continue;
                }
                c.setId(null);
                service.add(c);
                success++;
            } catch (Exception e) {
                fail++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("fail", fail);
        return result;
    }
}