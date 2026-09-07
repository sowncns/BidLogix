package com.qs.Backend.modules.crm.dashboard.service;

import com.qs.Backend.modules.crm.customer.repository.CustomerRepository;
import com.qs.Backend.modules.crm.dashboard.dto.DashboardStatsResponse;
import com.qs.Backend.platform.auth.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse stats() {
        return DashboardStatsResponse.builder()
                .totalCustomers(customerRepository.count())
                .totalUsers(accountRepository.count())
                .build();
    }
}
