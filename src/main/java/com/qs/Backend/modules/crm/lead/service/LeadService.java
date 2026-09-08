package com.qs.Backend.modules.crm.lead.service;

import com.qs.Backend.modules.crm.lead.dto.*;
import com.qs.Backend.modules.crm.lead.entity.Lead;
import com.qs.Backend.modules.crm.lead.repository.LeadRepository;
import com.qs.Backend.shared.exception.AppException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeadService {
    private final LeadRepository leadRepository;

    @Transactional(readOnly = true)
    public LeadListResponse list(String search, int limit, int offset) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        int safeOffset = Math.max(offset, 0);
        var page = leadRepository.findAll(spec(search), PageRequest.of(safeOffset / safeLimit, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt")));
        return LeadListResponse.builder().items(page.getContent().stream().map(this::toResponse).toList()).total(page.getTotalElements()).limit(safeLimit).offset(safeOffset).build();
    }

    @Transactional(readOnly = true)
    public LeadResponse get(String id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public LeadResponse create(LeadCreateRequest request) {
        Lead lead = new Lead();
        lead.setName(required(request.getName(), "name is required"));
        lead.setPhone(required(request.getPhone(), "phone is required"));
        lead.setEmail(blankToNull(request.getEmail()));
        lead.setBusinessField(blankToNull(request.getBusinessField()));
        lead.setNotes(blankToNull(request.getNotes()));
        lead.setServices(normalizeServices(request.getServices()));
        Instant now = Instant.now();
        lead.setCreatedAt(now);
        lead.setUpdatedAt(now);
        return toResponse(leadRepository.save(lead));
    }

    @Transactional
    public LeadResponse update(String id, LeadUpdateRequest request) {
        Lead lead = findOrThrow(id);
        if (request.getName() != null) lead.setName(required(request.getName(), "name cannot be empty"));
        if (request.getPhone() != null) lead.setPhone(required(request.getPhone(), "phone cannot be empty"));
        if (request.getEmail() != null) lead.setEmail(blankToNull(request.getEmail()));
        if (request.getBusinessField() != null) lead.setBusinessField(blankToNull(request.getBusinessField()));
        if (request.getNotes() != null) lead.setNotes(blankToNull(request.getNotes()));
        if (request.getServices() != null) lead.setServices(normalizeServices(request.getServices()));
        lead.setUpdatedAt(Instant.now());
        return toResponse(lead);
    }

    @Transactional
    public void delete(String id) {
        Lead lead = findOrThrow(id);
        lead.setDeletedAt(Instant.now());
        lead.setUpdatedAt(Instant.now());
    }

    private Specification<Lead> spec(String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(cb.like(cb.lower(root.get("name")), pattern), cb.like(cb.lower(root.get("phone")), pattern), cb.like(cb.lower(root.get("email")), pattern)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Lead findOrThrow(String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new AppException("Lead not found", HttpStatus.NOT_FOUND, "LEAD_NOT_FOUND");
        }
        return leadRepository.findById(uuid).filter(l -> l.getDeletedAt() == null).orElseThrow(() -> new AppException("Lead not found", HttpStatus.NOT_FOUND, "LEAD_NOT_FOUND"));
    }

    private LeadResponse toResponse(Lead lead) {
        return LeadResponse.builder().id(lead.getId().toString()).name(lead.getName()).phone(lead.getPhone()).email(lead.getEmail()).businessField(lead.getBusinessField()).notes(lead.getNotes()).services(lead.getServices()).createdAt(lead.getCreatedAt()).updatedAt(lead.getUpdatedAt()).build();
    }

    private List<String> normalizeServices(List<String> services) {
        if (services == null) return List.of();
        return services.stream().map(String::trim).filter(s -> !s.isBlank()).distinct().toList();
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) throw new AppException(message, HttpStatus.BAD_REQUEST, "LEAD_INVALID_REQUEST");
        return value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
