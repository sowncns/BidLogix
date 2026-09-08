package com.qs.Backend.platform.user.service;

import com.qs.Backend.platform.auth.entity.AuthUser;
import com.qs.Backend.platform.auth.entity.Permission;
import com.qs.Backend.platform.auth.entity.Role;
import com.qs.Backend.platform.auth.repository.AccountRepository;
import com.qs.Backend.platform.auth.repository.RoleRepository;
import com.qs.Backend.platform.auth.repository.SessionRepository;
import com.qs.Backend.platform.logging.audit.service.AuditLogService;
import com.qs.Backend.platform.permission.entity.AccountOrganization;
import com.qs.Backend.platform.permission.repository.AccountOrganizationRepository;
import com.qs.Backend.platform.permission.service.PermissionService;
import com.qs.Backend.platform.profile.entity.Profile;
import com.qs.Backend.platform.profile.service.ProfileService;
import com.qs.Backend.platform.user.dto.*;
import com.qs.Backend.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

// Ports qs-crm's internal/core/user (+ its profile/role/organization aggregation done at the
// HTTP layer in internal/adapters/http/user). AuthUser already unifies "auth identity" and
// "user" in this codebase, so this service manages AuthUser directly instead of duplicating
// it behind a separate User entity.
@Service
@RequiredArgsConstructor
public class UserAdminService {

    // Permissions that unlock the corresponding email notification toggle - mirrors
    // qs-crm's emailNotificationDefaultsForPermissions.
    private static final String PERMISSION_ACTIVATION_APPROVE = "PRODUCT_ITEM_APPROVE_ACTIVATION";
    private static final String PERMISSION_WORK_ORDER_READ = "WORK_ORDER_READ";

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final SessionRepository sessionRepository;
    private final AccountOrganizationRepository accountOrganizationRepository;
    private final ProfileService profileService;
    private final PermissionService permissionService;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Transactional
    public UserResponse create(UserCreateRequest request, UUID performedBy) {
        UserRequestValidator.normalize(request);

        String username = request.getEmail() != null && !request.getEmail().isEmpty() ? request.getEmail() : request.getPhone();
        if (accountRepository.existsByUsername(username)) {
            throw new AppException("Email hoặc số điện thoại đã được sử dụng", HttpStatus.CONFLICT, "USER_ALREADY_EXISTS");
        }

        AuthUser account = new AuthUser();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setEmail(request.getEmail());
        account.setPhone(request.getPhone());
        account.setRegion(request.getRegion());
        account.setActive(true);
        account.setRoles(resolveRoles(request.getRoleIds()));
        accountRepository.save(account);

        profileService.upsert(account.getId(), request.getFirstName(), request.getLastName(), request.getAvatarUrl());

        if (request.getOrganizationId() != null) {
            assignOrganization(account.getId(), request.getOrganizationId());
        }

        applyEmailNotificationDefaults(account);

        auditLogService.log(performedBy, null, "USER_CREATE", "AuthUser", String.valueOf(account.getId()), null);

        return toResponse(account);
    }

    @Transactional
    public UserResponse update(UUID id, UserUpdateRequest request, UUID performedBy) {
        UserRequestValidator.normalize(request);

        AuthUser account = findOrThrow(id);
        if (request.getEmail() != null) account.setEmail(request.getEmail());
        if (request.getPhone() != null) account.setPhone(request.getPhone());
        if (request.getRegion() != null) account.setRegion(request.getRegion());

        if (request.getFirstName() != null || request.getLastName() != null || request.getAvatarUrl() != null) {
            profileService.upsert(id, request.getFirstName(), request.getLastName(), request.getAvatarUrl());
        }

        if (request.getOrganizationId() != null) {
            accountOrganizationRepository.deleteByAccountId(id);
            assignOrganization(id, request.getOrganizationId());
        }

        if (request.getRoleIds() != null) {
            account.setRoles(resolveRoles(request.getRoleIds()));
            accountRepository.save(account);
            applyEmailNotificationDefaults(account);
        }

        account.setUpdatedAt(Instant.now());
        accountRepository.save(account);

        auditLogService.log(performedBy, null, "USER_UPDATE", "AuthUser", String.valueOf(id), null);

        return toResponse(account);
    }

    @Transactional
    public void changePassword(UUID id, UserChangePasswordRequest request, UUID performedBy) {
        UserRequestValidator.validateNewPassword(request.getNewPassword(), request.getConfirmPassword());

        AuthUser account = findOrThrow(id);
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
        sessionRepository.revokeAllForAccount(id, Instant.now());

        auditLogService.log(performedBy, null, "USER_CHANGE_PASSWORD_BY_ADMIN", "AuthUser", String.valueOf(id), null);
    }

    @Transactional
    public UserResponse updateEmailNotifications(UUID id, UpdateEmailNotificationsRequest prefs) {
        AuthUser account = findOrThrow(id);

        if (prefs.getActivationRequest() != null) {
            if (prefs.getActivationRequest() && !permissionService.hasPermission(id, PERMISSION_ACTIVATION_APPROVE)) {
                throw new AppException("Yêu cầu quyền duyệt kích hoạt để bật thông báo này", HttpStatus.BAD_REQUEST, "USER_INVALID");
            }
            account.setEmailNotifyActivationRequest(prefs.getActivationRequest());
        }
        if (prefs.getServiceRequest() != null) {
            if (prefs.getServiceRequest() && !permissionService.hasPermission(id, PERMISSION_WORK_ORDER_READ)) {
                throw new AppException("Yêu cầu quyền xem work order để bật thông báo này", HttpStatus.BAD_REQUEST, "USER_INVALID");
            }
            account.setEmailNotifyServiceRequest(prefs.getServiceRequest());
        }
        accountRepository.save(account);
        return toResponse(account);
    }

    @Transactional
    public void delete(UUID id, UUID currentUserId) {
        if (id.equals(currentUserId)) {
            throw new AppException("Không thể tự xóa chính mình", HttpStatus.BAD_REQUEST, "USER_SELF_DELETE");
        }
        AuthUser account = findOrThrow(id);
        account.setActive(false);
        account.setUpdatedAt(Instant.now());
        accountRepository.save(account);

        auditLogService.log(currentUserId, null, "USER_DELETE", "AuthUser", String.valueOf(id), null);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public UserListResponse list(UserFilterParams filters, int limit, int offset) {
        Specification<AuthUser> spec = toSpecification(filters);
        List<AuthUser> matched = accountRepository.findAll(spec);
        long total = matched.size();

        List<UserResponse> items = matched.stream()
                .skip(Math.max(offset, 0))
                .limit(Math.max(limit, 0))
                .map(this::toResponse)
                .toList();

        return UserListResponse.builder()
                .items(items)
                .limit(limit)
                .offset(offset)
                .total(total)
                .build();
    }

    // ---- Internal helpers ----

    private AuthUser findOrThrow(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AppException("Tài khoản không tồn tại", HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));
    }

    private Set<Role> resolveRoles(List<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(roleRepository.findAllById(roleIds));
    }

    private void assignOrganization(UUID accountId, UUID organizationId) {
        if (accountOrganizationRepository.existsByAccountIdAndOrganizationId(accountId, organizationId)) {
            return;
        }
        AccountOrganization link = new AccountOrganization();
        link.setAccountId(accountId);
        link.setOrganizationId(organizationId);
        accountOrganizationRepository.save(link);
    }

    private void applyEmailNotificationDefaults(AuthUser account) {
        account.setEmailNotifyActivationRequest(permissionService.hasPermission(account.getId(), PERMISSION_ACTIVATION_APPROVE));
        account.setEmailNotifyServiceRequest(permissionService.hasPermission(account.getId(), PERMISSION_WORK_ORDER_READ));
        accountRepository.save(account);
    }

    private Specification<AuthUser> toSpecification(UserFilterParams filters) {
        return (root, query, cb) -> {
            query.distinct(true);
            var predicate = cb.conjunction();

            if (filters.keyword() != null && !filters.keyword().isBlank()) {
                String like = "%" + filters.keyword().trim().toLowerCase() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(cb.lower(root.get("username")), like),
                        cb.like(cb.lower(root.get("email")), like),
                        cb.like(cb.lower(root.get("phone")), like)
                ));
            }
            if (filters.createdAtFrom() != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("createdAt"), filters.createdAtFrom()));
            }
            if (filters.createdAtTo() != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("createdAt"), filters.createdAtTo()));
            }
            if (filters.roleCode() != null && !filters.roleCode().isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.join("roles").get("code"), filters.roleCode()));
            }
            if (filters.permissionCode() != null && !filters.permissionCode().isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.join("roles").join("permissions").get("code"), filters.permissionCode()));
            }
            return predicate;
        };
    }

    private UserResponse toResponse(AuthUser account) {
        Profile profile = profileService.getOrEmpty(account.getId());
        List<RoleInfo> roleInfos = account.getRoles().stream()
                .map(r -> new RoleInfo(r.getId(), r.getName(), r.getCode()))
                .toList();
        List<PermissionInfo> permissionInfos = account.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .distinct()
                .map((Permission p) -> new PermissionInfo(p.getId(), p.getCode(), p.getDescription()))
                .toList();
        UUID organizationId = accountOrganizationRepository.findByAccountId(account.getId()).stream()
                .findFirst()
                .map(AccountOrganization::getOrganizationId)
                .orElse(null);

        String fullName = ((profile.getLastName() == null ? "" : profile.getLastName()) + " " +
                (profile.getFirstName() == null ? "" : profile.getFirstName())).trim();

        return UserResponse.builder()
                .id(account.getId())
                .email(account.getEmail())
                .phone(account.getPhone())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .fullName(fullName)
                .avatarUrl(profile.getAvatarUrl())
                .region(account.getRegion())
                .organizationId(organizationId)
                .roles(roleInfos)
                .permissions(permissionInfos)
                .emailNotifications(new EmailNotificationPrefsResponse(account.isEmailNotifyActivationRequest(), account.isEmailNotifyServiceRequest()))
                .createdAt(account.getCreatedAt())
                .build();
    }
}
