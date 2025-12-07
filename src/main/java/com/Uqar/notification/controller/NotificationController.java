package com.Uqar.notification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Uqar.notification.dto.DeviceTokenRequest;
import com.Uqar.notification.dto.NotificationRequest;
import com.Uqar.notification.dto.NotificationResponse;
import com.Uqar.notification.entity.DeviceToken;
import com.Uqar.notification.service.DeviceTokenService;
import com.Uqar.notification.service.NotificationService;
import com.Uqar.product.dto.PaginationDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification Management", description = "APIs for managing notifications and device tokens")
@CrossOrigin("*")
public class NotificationController {
    
    private final NotificationService notificationService;
    private final DeviceTokenService deviceTokenService;
    
    public NotificationController(NotificationService notificationService, 
                                 DeviceTokenService deviceTokenService) {
        this.notificationService = notificationService;
        this.deviceTokenService = deviceTokenService;
    }
    
    @PostMapping("/register-token")
    @Operation(summary = "Register device token", description = "Register a device token for push notifications")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Device token registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<DeviceToken> registerDeviceToken(
            @Parameter(description = "Device token request", required = true)
            @Valid @RequestBody DeviceTokenRequest request) {
        Long currentUserId = notificationService.getCurrentUserId();
        DeviceToken deviceToken = deviceTokenService.registerDeviceToken(currentUserId, request);
        return ResponseEntity.ok(deviceToken);
    }   
    
    @DeleteMapping("/unregister-token/{deviceToken}")
    @Operation(summary = "Unregister device token", description = "Unregister a device token")
    public ResponseEntity<Void> unregisterDeviceToken(
            @Parameter(description = "Device token to unregister")
            @PathVariable String deviceToken) {
        Long currentUserId = notificationService.getCurrentUserId();
        deviceTokenService.unregisterDeviceToken(currentUserId, deviceToken);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping
    @Operation(summary = "Get user notifications", description = "Get all notifications for the current user with enhanced pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PaginationDTO<NotificationResponse>> getUserNotifications(
            @Parameter(description = "Page number (0-based)", example = "0") 
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") 
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size) {
        PaginationDTO<NotificationResponse> notifications = notificationService.getUserNotificationsPaginated(page, size);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications", description = "Get all unread notifications for the current user with enhanced pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved unread notifications"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PaginationDTO<NotificationResponse>> getUnreadNotifications(
            @Parameter(description = "Page number (0-based)", example = "0") 
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") 
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size) {
        PaginationDTO<NotificationResponse> notifications = notificationService.getUnreadNotificationsPaginated(page, size);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/unread/count")
    @Operation(summary = "Get unread notifications count", description = "Get count of unread notifications")
    public ResponseEntity<Long> getUnreadCount() {
        Long count = notificationService.getUnreadCount();
        return ResponseEntity.ok(count);
    }
    
    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Mark a notification as read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @Parameter(description = "Notification ID")
            @PathVariable Long id) {
        NotificationResponse response = notificationService.markAsRead(id);
        return ResponseEntity.ok(response);
    }
    
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @PostMapping("/send")
    @Operation(summary = "Send notification", description = "Send a notification to a user (Admin only)")
    public ResponseEntity<NotificationResponse> sendNotification(
            @Parameter(description = "Notification request", required = true)
            @Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.sendNotification(request);
        return ResponseEntity.ok(response);
    }
}


