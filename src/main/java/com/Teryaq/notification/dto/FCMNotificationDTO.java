package com.Teryaq.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FCMNotificationDTO {
    
    private String title;
    private String body;
    private Map<String, Object> data;
    private String imageUrl; // اختياري
    private String sound; // اختياري
    private String clickAction; // اختياري
}


