package com.qs.Backend.platform.user.dto;

import lombok.Getter;
import lombok.Setter;

// null = leave that toggle alone.
@Getter
@Setter
public class UpdateEmailNotificationsRequest {
    private Boolean activationRequest;
    private Boolean serviceRequest;
}
