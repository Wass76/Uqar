package com.Teryaq.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeviceTokenRequest {
    
    @NotBlank(message = "Device token is required")
    private String deviceToken;
    
    private String deviceType; // ANDROID, IOS, WEB
}


