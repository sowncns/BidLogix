package com.qs.Backend.modules.crm.student.controller;

import com.qs.Backend.modules.crm.student.dto.StudentCreateRequest;
import com.qs.Backend.modules.crm.student.dto.StudentListResponse;
import com.qs.Backend.modules.crm.student.dto.StudentResponse;
import com.qs.Backend.modules.crm.student.dto.StudentUpdateRequest;
import com.qs.Backend.modules.crm.student.service.StudentService;
import com.qs.Backend.platform.captcha.service.CaptchaService;
import com.qs.Backend.shared.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;
    private final CaptchaService captchaService;

    @GetMapping("/students")
    public ApiResponse<StudentListResponse> list(@RequestParam(required = false) String search, @RequestParam(required = false) String status, @RequestParam(defaultValue = "50") int limit, @RequestParam(defaultValue = "0") int offset) {
        return ApiResponse.ok(studentService.list(search, status, limit, offset), null);
    }

    @GetMapping("/students/{id}")
    public ApiResponse<StudentResponse> get(@PathVariable String id) { return ApiResponse.ok(studentService.get(id), null); }

    @PostMapping("/students")
    public ApiResponse<StudentResponse> create(@RequestBody StudentCreateRequest request) { return ApiResponse.created(studentService.create(request), "Student created"); }

    // Public registration form: gated behind a self-hosted captcha, matching
    // the Go backend (id/answer travel as headers, not body fields).
    @PostMapping("/public/students")
    public ApiResponse<StudentResponse> createPublic(@RequestBody StudentCreateRequest request, HttpServletRequest httpRequest) {
        captchaService.verify(httpRequest.getHeader("X-Captcha-Id"), httpRequest.getHeader("X-Captcha-Answer"));
        return ApiResponse.created(studentService.create(request), "Student created");
    }

    @PatchMapping("/students/{id}")
    public ApiResponse<StudentResponse> update(@PathVariable String id, @RequestBody StudentUpdateRequest request) { return ApiResponse.ok(studentService.update(id, request), "Student updated"); }

    @DeleteMapping("/students/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) { studentService.delete(id); return ApiResponse.ok(null, "Student deleted"); }
}
