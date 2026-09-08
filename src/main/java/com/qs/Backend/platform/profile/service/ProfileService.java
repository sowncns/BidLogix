package com.qs.Backend.platform.profile.service;

import com.qs.Backend.platform.profile.dto.ProfileResponse;
import com.qs.Backend.platform.profile.dto.ProfileUpdateRequest;
import com.qs.Backend.platform.profile.entity.Profile;
import com.qs.Backend.platform.profile.repository.ProfileRepository;
import com.qs.Backend.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    @Transactional(readOnly = true)
    public ProfileResponse getMe(Long userId) {
        requireUser(userId);
        return toResponse(getOrEmpty(userId));
    }

    public Profile getOrEmpty(Long userId) {
        return profileRepository.findByUserId(userId).orElseGet(() -> {
            Profile empty = new Profile();
            empty.setUserId(userId);
            return empty;
        });
    }

    @Transactional
    public ProfileResponse updateMe(Long userId, ProfileUpdateRequest request) {
        requireUser(userId);
        Profile profile = upsert(userId, request.getFirstName(), request.getLastName(), request.getAvatarUrl());
        if (request.getRegion() != null) profile.setRegion(blankToNull(request.getRegion()));
        profile.setUpdatedAt(Instant.now());
        return toResponse(profileRepository.save(profile));
    }

    // Create-or-update: mirrors qs-crm's "get or create profile" flow on user update.
    public Profile upsert(Long userId, String firstName, String lastName, String avatarUrl) {
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
        return ProfileResponse.builder().id(profile.getId()).userId(profile.getUserId()).firstName(profile.getFirstName()).lastName(profile.getLastName()).avatarUrl(profile.getAvatarUrl()).region(profile.getRegion()).createdAt(profile.getCreatedAt()).updatedAt(profile.getUpdatedAt()).build();
    }

    private void requireUser(Long userId) {
        if (userId == null) throw new AppException("User not authenticated", HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED");
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
