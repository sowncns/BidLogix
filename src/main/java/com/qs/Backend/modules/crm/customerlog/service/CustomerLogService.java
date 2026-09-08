package com.qs.Backend.modules.crm.customerlog.service;

import com.qs.Backend.modules.crm.customerlog.dto.CustomerLogListResponse;
import com.qs.Backend.modules.crm.customerlog.dto.CustomerLogResponse;
import com.qs.Backend.modules.crm.customerlog.entity.CustomerLog;
import com.qs.Backend.modules.crm.customerlog.repository.CustomerLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerLogService {
    private final CustomerLogRepository customerLogRepository;

    @Transactional(readOnly = true)
    public CustomerLogListResponse list(String customerId, int limit, int offset) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        var page = customerLogRepository.findByCustomerId(customerId, PageRequest.of(offset / safeLimit, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt")));
        return CustomerLogListResponse.builder().logs(page.map(this::toResponse).getContent()).limit(limit).offset(Math.max(offset, 0)).build();
    }

    @Transactional
    public void log(String customerId, Long actorId, String action, String targetType, String targetId, List<CustomerLog.FieldChange> changes) {
        if (customerId == null || customerId.isBlank() || action == null || action.isBlank()) return;
        CustomerLog log = new CustomerLog();
        log.setCustomerId(customerId);
        log.setActorId(actorId == null ? null : String.valueOf(actorId));
        log.setAction(action);
        log.setTargetType(targetType == null || targetType.isBlank() ? "customer" : targetType);
        log.setTargetId(targetId);
        log.setChanges(changes == null ? List.of() : changes);
        customerLogRepository.save(log);
    }

    private CustomerLogResponse toResponse(CustomerLog log) {
        return CustomerLogResponse.builder()
                .id(log.getId())
                .actorId(log.getActorId())
                .action(log.getAction())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .changes(log.getChanges().stream().map(c -> CustomerLogResponse.FieldChangeResponse.builder().field(c.getField()).old(c.getOld()).newValue(c.getNewValue()).build()).toList())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
