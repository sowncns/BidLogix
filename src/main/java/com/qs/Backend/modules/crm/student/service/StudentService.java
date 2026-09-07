package com.qs.Backend.modules.crm.student.service;

import com.qs.Backend.modules.crm.student.dto.StudentCreateRequest;
import com.qs.Backend.modules.crm.student.dto.StudentListResponse;
import com.qs.Backend.modules.crm.student.dto.StudentResponse;
import com.qs.Backend.modules.crm.student.dto.StudentUpdateRequest;
import com.qs.Backend.modules.crm.student.entity.Student;
import com.qs.Backend.modules.crm.student.repository.StudentRepository;
import com.qs.Backend.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StudentService {
    private static final Set<String> STATUSES = Set.of("registered", "deposited", "enrolled", "cancelled");
    private final StudentRepository studentRepository;

    @Transactional(readOnly = true)
    public StudentListResponse list(String search, String status, int limit, int offset) {
        if (status != null && !status.isBlank() && !STATUSES.contains(status.trim())) throw invalid("unknown status: " + status.trim());
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        Specification<Student> spec = (root, query, cb) -> {
            var predicate = cb.isNull(root.get("deletedAt"));
            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                predicate = cb.and(predicate, cb.or(cb.like(cb.lower(root.get("fullName")), like), cb.like(cb.lower(root.get("phone")), like), cb.like(cb.lower(root.get("email")), like)));
            }
            if (status != null && !status.isBlank()) predicate = cb.and(predicate, cb.equal(root.get("status"), status.trim()));
            return predicate;
        };
        var page = studentRepository.findAll(spec, PageRequest.of(offset / safeLimit, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt")));
        return StudentListResponse.builder().items(page.map(this::toResponse).getContent()).total(page.getTotalElements()).limit(limit).offset(Math.max(offset, 0)).build();
    }

    @Transactional(readOnly = true)
    public StudentResponse get(String id) { return toResponse(findActiveOrThrow(id)); }

    @Transactional
    public StudentResponse create(StudentCreateRequest request) {
        validateCreate(request);
        Student student = new Student();
        student.setFullName(request.getFullName().trim());
        student.setEmail(trim(request.getEmail()));
        student.setPhone(request.getPhone().trim());
        student.setBusinessField(trim(request.getBusinessField()));
        student.setDepositAgreed(request.isDepositAgreed());
        student.setStatus(request.getStatus() == null || request.getStatus().isBlank() ? "registered" : request.getStatus().trim());
        student.setNotes(trim(request.getNotes()));
        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse update(String id, StudentUpdateRequest request) {
        Student student = findActiveOrThrow(id);
        if (request.getFullName() != null) {
            String fullName = trim(request.getFullName());
            if (fullName == null || fullName.isEmpty()) throw invalid("full_name cannot be empty");
            student.setFullName(fullName);
        }
        if (request.getEmail() != null) student.setEmail(trim(request.getEmail()));
        if (request.getPhone() != null) {
            String phone = trim(request.getPhone());
            if (phone == null || phone.isEmpty()) throw invalid("phone cannot be empty");
            student.setPhone(phone);
        }
        if (request.getBusinessField() != null) student.setBusinessField(trim(request.getBusinessField()));
        if (request.getDepositAgreed() != null) student.setDepositAgreed(request.getDepositAgreed());
        if (request.getStatus() != null) {
            String status = trim(request.getStatus());
            if (!STATUSES.contains(status)) throw invalid("unknown status: " + status);
            student.setStatus(status);
        }
        if (request.getNotes() != null) student.setNotes(trim(request.getNotes()));
        student.setUpdatedAt(Instant.now());
        return toResponse(student);
    }

    @Transactional
    public void delete(String id) {
        Student student = findActiveOrThrow(id);
        student.setDeletedAt(Instant.now());
        student.setUpdatedAt(Instant.now());
    }

    private void validateCreate(StudentCreateRequest request) {
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) throw invalid("full_name is required");
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) throw invalid("phone is required");
        if (!request.isDepositAgreed()) throw invalid("deposit_agreed must be accepted");
        if (request.getStatus() != null && !request.getStatus().isBlank() && !STATUSES.contains(request.getStatus().trim())) throw invalid("unknown status: " + request.getStatus().trim());
    }

    private Student findActiveOrThrow(String id) {
        if (id == null || id.isBlank()) throw invalid("id is required");
        Student student = studentRepository.findById(id).orElseThrow(() -> new AppException("student not found", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        if (student.getDeletedAt() != null) throw new AppException("student not found", HttpStatus.NOT_FOUND, "NOT_FOUND");
        return student;
    }

    private AppException invalid(String message) { return new AppException("invalid student: " + message, HttpStatus.BAD_REQUEST, "INVALID_INPUT"); }
    private String trim(String value) { return value == null ? null : value.trim(); }
    private StudentResponse toResponse(Student s) { return StudentResponse.builder().id(s.getId()).fullName(s.getFullName()).email(s.getEmail()).phone(s.getPhone()).businessField(s.getBusinessField()).depositAgreed(s.isDepositAgreed()).status(s.getStatus()).notes(s.getNotes()).createdAt(s.getCreatedAt()).updatedAt(s.getUpdatedAt()).build(); }
}
