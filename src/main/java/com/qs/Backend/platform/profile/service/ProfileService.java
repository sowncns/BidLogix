package com.qs.Backend.platform.profile.service;

import com.qs.Backend.platform.auth.entity.AuthUser;
import com.qs.Backend.platform.auth.repository.AccountRepository;
import com.qs.Backend.platform.file.FileStorageService;
import com.qs.Backend.platform.file.entity.StoredFile;
import com.qs.Backend.platform.file.repository.StoredFileRepository;
import com.qs.Backend.platform.profile.dto.ProfileResponse;
import com.qs.Backend.platform.profile.dto.ProfileUpdateRequest;
import com.qs.Backend.platform.profile.entity.Profile;
import com.qs.Backend.platform.profile.repository.ProfileRepository;
import com.qs.Backend.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final FileStorageService fileStorageService;
    private final StoredFileRepository storedFileRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public ProfileResponse getMe(UUID userId) {
        requireUser(userId);
        return toResponse(getOrEmpty(userId));
    }

    @Transactional(readOnly = true)
    public ProfileResponse getByUserId(UUID userId) {
        requireUser(userId);
        return toResponse(getOrEmpty(userId));
    }

    public Profile getOrEmpty(UUID userId) {
        return profileRepository.findByUserId(userId).orElseGet(() -> {
            Profile empty = new Profile();
            empty.setUserId(userId);
            return empty;
        });
    }

    @Transactional
    public ProfileResponse updateMe(UUID userId, ProfileUpdateRequest request) {
        requireUser(userId);
        Profile profile = upsert(userId, request.getFirstName(), request.getLastName(), request.getAvatarUrl());
        updateRegion(userId, request.getRegion());
        profile.setUpdatedAt(Instant.now());
        return toResponse(profileRepository.save(profile));
    }

    @Transactional
    public ProfileResponse updateByUserId(UUID userId, ProfileUpdateRequest request) {
        requireUser(userId);
        Profile profile = upsert(userId, request.getFirstName(), request.getLastName(), request.getAvatarUrl());
        updateRegion(userId, request.getRegion());
        profile.setUpdatedAt(Instant.now());
        return toResponse(profileRepository.save(profile));
    }

    // region lives on auth_users (qs_crm schema), not user_profiles.
    private void updateRegion(UUID userId, String region) {
        if (region == null) return;
        accountRepository.findById(userId).ifPresent(account -> {
            account.setRegion(blankToNull(region));
            accountRepository.save(account);
        });
    }

    @Transactional
    public ProfileResponse uploadAvatar(UUID userId, MultipartFile file, UUID uploadedBy) {
        requireUser(userId);
        String storageKey = fileStorageService.storeFile(file, "avatars/" + userId);
        StoredFile storedFile = new StoredFile();
        storedFile.setOriginalName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "avatar");
        storedFile.setStorageKey(storageKey);
        storedFile.setMimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        storedFile.setSizeBytes(file.getSize());
        storedFile.setVisibility("internal");
        storedFile.setUploadedBy(uploadedBy != null ? uploadedBy : userId);
        storedFile = storedFileRepository.save(storedFile);

        Profile profile = getOrEmpty(userId);
        profile.setAvatarUrl("/users/" + userId + "/avatar/" + storedFile.getId());
        profile.setUpdatedAt(Instant.now());
        return toResponse(profileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public StoredFile findAvatarFile(UUID userId, String fileId) {
        requireUser(userId);
        StoredFile storedFile = storedFileRepository.findById(UUID.fromString(fileId))
                .orElseThrow(() -> new AppException("Không tìm thấy avatar", HttpStatus.NOT_FOUND, "AVATAR_NOT_FOUND"));
        String expectedPrefix = "avatars/" + userId + "/";
        if (storedFile.getDeletedAt() != null || !storedFile.getStorageKey().startsWith(expectedPrefix)) {
            throw new AppException("Không tìm thấy avatar", HttpStatus.NOT_FOUND, "AVATAR_NOT_FOUND");
        }
        return storedFile;
    }

    public Resource loadAvatar(StoredFile storedFile) {
        return fileStorageService.loadAsResource(storedFile.getStorageKey());
    }

    // Create-or-update: mirrors qs-crm's "get or create profile" flow on user update.
    public Profile upsert(UUID userId, String firstName, String lastName, String avatarUrl) {
        Profile profile = profileRepository.findByUserId(userId).orElseGet(() -> {
            Profile created = new Profile();
            created.setUserId(userId);
            return created;
        });
        if (firstName != null) profile.setFirstName(firstName);
        if (lastName != null) profile.setLastName(lastName);
        if (avatarUrl != null) profile.setAvatarUrl(avatarUrl);
        profile.setUpdatedAt(Instant.now());
        return profileRepository.save(profile);
    }

    private ProfileResponse toResponse(Profile profile) {
        String region = accountRepository.findById(profile.getUserId()).map(AuthUser::getRegion).orElse(null);
        return ProfileResponse.builder().id(profile.getId()).userId(profile.getUserId()).firstName(profile.getFirstName()).lastName(profile.getLastName()).avatarUrl(profile.getAvatarUrl()).region(region).createdAt(profile.getCreatedAt()).updatedAt(profile.getUpdatedAt()).build();
    }

    private void requireUser(UUID userId) {
        if (userId == null) throw new AppException("User not authenticated", HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED");
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
