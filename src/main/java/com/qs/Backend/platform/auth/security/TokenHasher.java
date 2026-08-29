package com.qs.Backend.platform.auth.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

// Deterministic hash for opaque tokens (refresh token, password reset token) so they can be
// looked up by value without storing the raw secret. Not for passwords - use PasswordEncoder for those.
public final class TokenHasher {

    private TokenHasher() {}

    public static String sha256(String rawValue) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawValue.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
