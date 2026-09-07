package com.qs.Backend.modules.crm.metadata.service;

import com.qs.Backend.modules.crm.businessfield.repository.BusinessFieldRepository;
import com.qs.Backend.modules.crm.metadata.dto.CustomerMetadataResponse;
import com.qs.Backend.modules.crm.metadata.dto.LeadMetadataResponse;
import com.qs.Backend.modules.crm.metadata.dto.MetadataOption;
import com.qs.Backend.modules.crm.metadata.dto.UsersMetadataResponse;
import com.qs.Backend.platform.auth.entity.AuthUser;
import com.qs.Backend.platform.auth.repository.AccountRepository;
import com.qs.Backend.platform.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MetadataService {
    private final BusinessFieldRepository businessFieldRepository;
    private final OrganizationRepository organizationRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public CustomerMetadataResponse customerMetadata() {
        List<MetadataOption> users = users();
        return CustomerMetadataResponse.builder()
                .customerGroups(List.of())
                .customerStatuses(List.of())
                .businessFields(businessFieldRepository.findAll().stream()
                        .map(field -> MetadataOption.builder().id(field.getId()).name(field.getName()).build())
                        .toList())
                .organizations(organizations())
                .users(users)
                .assignableUsers(users)
                .build();
    }

    @Transactional(readOnly = true)
    public LeadMetadataResponse leadMetadata() {
        return LeadMetadataResponse.builder()
                .organizations(organizations())
                .users(users())
                .build();
    }

    @Transactional(readOnly = true)
    public UsersMetadataResponse usersMetadata() {
        return UsersMetadataResponse.builder().users(users()).build();
    }

    private List<MetadataOption> organizations() {
        return organizationRepository.findAll().stream()
                .map(org -> MetadataOption.builder().id(String.valueOf(org.getId())).name(org.getName()).code(org.getCode()).build())
                .toList();
    }

    private List<MetadataOption> users() {
        return accountRepository.findAll().stream()
                .filter(AuthUser::isActive)
                .map(user -> MetadataOption.builder().id(String.valueOf(user.getId())).name(user.getUsername()).build())
                .toList();
    }
}
