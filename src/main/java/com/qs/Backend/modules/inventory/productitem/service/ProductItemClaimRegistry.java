package com.qs.Backend.modules.inventory.productitem.service;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProductItemClaimRegistry {

    private static final Duration CLAIM_TTL = Duration.ofSeconds(60);

    private final Map<String, Claim> claims = new ConcurrentHashMap<>();

    public synchronized Map<String, UUID> sync(UUID userId, List<String> chipIds) {
        sweepExpired();
        Set<String> wanted = new HashSet<>(chipIds);
        claims.entrySet().removeIf(entry -> entry.getValue().userId().equals(userId) && !wanted.contains(entry.getKey()));

        Map<String, UUID> conflicts = new HashMap<>();
        Instant expiresAt = Instant.now().plus(CLAIM_TTL);
        for (String chipId : wanted) {
            Claim existing = claims.get(chipId);
            if (existing != null && !existing.expired() && !existing.userId().equals(userId)) {
                conflicts.put(chipId, existing.userId());
                continue;
            }
            claims.put(chipId, new Claim(userId, expiresAt));
        }
        return conflicts;
    }

    public synchronized boolean acquire(UUID userId, String chipId) {
        sweepExpired();
        Claim existing = claims.get(chipId);
        if (existing != null && !existing.expired() && !existing.userId().equals(userId)) {
            return false;
        }
        claims.put(chipId, new Claim(userId, Instant.now().plus(CLAIM_TTL)));
        return true;
    }

    public synchronized void release(UUID userId, String chipId) {
        Claim existing = claims.get(chipId);
        if (existing != null && existing.userId().equals(userId)) {
            claims.remove(chipId);
        }
    }

    public synchronized void releaseAll(UUID userId) {
        claims.entrySet().removeIf(entry -> entry.getValue().userId().equals(userId));
    }

    private void sweepExpired() {
        claims.entrySet().removeIf(entry -> entry.getValue().expired());
    }

    private record Claim(UUID userId, Instant expiresAt) {
        boolean expired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
