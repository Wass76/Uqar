package com.Teryaq.notification.mapper;

import com.Teryaq.notification.dto.NotificationResponse;
import com.Teryaq.notification.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    
    public NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
            .id(notification.getId())
            .userId(notification.getUser().getId())
            .title(notification.getTitle())
            .body(notification.getBody())
            .notificationType(notification.getNotificationType())
            .data(notification.getData())
            .sentAt(notification.getSentAt())
            .readAt(notification.getReadAt())
            .status(notification.getStatus())
            .createdAt(notification.getCreatedAt())
            .build();
    }
}


