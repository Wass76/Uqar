package com.Teryaq.notification.dto;

import com.Teryaq.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    
    private Long id;
    private Long userId;
    private String title;
    private String body;
    private NotificationType notificationType;
    private Map<String, Object> data;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private String status;
    private LocalDateTime createdAt;
}


