package com.qs.Backend.platform.auth.service;

// Best-effort async fan-out of an OTP code to phone channels (Zalo, WhatsApp). Never throws to the
// caller - failures are logged only. Default impl is a no-op until a delivery provider is wired up.
public interface PhoneOtpDispatcher {
    void dispatch(Long accountId, String rawPhone, String code, int expiryMinutes);
}
