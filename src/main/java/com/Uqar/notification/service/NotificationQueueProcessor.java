package com.Uqar.notification.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.Uqar.notification.dto.FCMNotificationDTO;
import com.Uqar.notification.entity.DeviceToken;
import com.Uqar.notification.entity.Notification;
import com.Uqar.notification.repository.DeviceTokenRepository;
import com.Uqar.notification.repository.NotificationRepository;

/**
 * Processes pending notifications from the database queue.
 * Runs every 5 seconds to check for PENDING notifications and send them via FCM.
 */
@Component
public class NotificationQueueProcessor {

    private static final Logger logger = LoggerFactory.getLogger(NotificationQueueProcessor.class);
    
    private static final int MAX_RETRY_COUNT = 3;
    private static final int BATCH_SIZE = 10; // Process 10 notifications at a time
    
    // Exponential backoff delays (in minutes)
    private static final int[] RETRY_DELAYS_MINUTES = {0, 5, 15, 30}; // 0, 5, 15, 30 minutes

    private final NotificationRepository notificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final FirebaseMessagingService firebaseMessagingService;

    @Autowired(required = false)
    public NotificationQueueProcessor(NotificationRepository notificationRepository,
                                    DeviceTokenRepository deviceTokenRepository,
                                    FirebaseMessagingService firebaseMessagingService) {
        this.notificationRepository = notificationRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.firebaseMessagingService = firebaseMessagingService;
        
        if (firebaseMessagingService == null) {
            logger.warn("FirebaseMessagingService is not available. Notifications will be queued but not sent.");
            logger.warn("Please check Firebase configuration and ensure firebase.messaging.enabled=true");
        } else {
            logger.info("NotificationQueueProcessor initialized with FirebaseMessagingService");
        }
    }

    /**
     * Processes pending notifications every 5 seconds.
     * Fixed delay ensures we don't overwhelm the system.
     */
    @Scheduled(fixedDelay = 5000) // 5 seconds
    @Transactional
    public void processPendingNotifications() {
        try {
            LocalDateTime now = LocalDateTime.now();
            Pageable pageable = PageRequest.of(0, BATCH_SIZE);
            
            // Get notifications that are ready for retry (next_retry_at <= now or null)
            List<Notification> readyNotifications = notificationRepository
                .findReadyForRetry("PENDING", now, pageable)
                .getContent();

            if (readyNotifications.isEmpty()) {
                return; // No notifications ready for processing
            }

            logger.debug("Processing {} notifications ready for retry", readyNotifications.size());

            for (Notification notification : readyNotifications) {
                try {
                    // Skip if next_retry_at is in the future
                    if (notification.getNextRetryAt() != null && notification.getNextRetryAt().isAfter(now)) {
                        continue;
                    }
                    processNotification(notification);
                } catch (Exception e) {
                    logger.error("Error processing notification {}: {}", 
                        notification.getId(), e.getMessage(), e);
                    handleNotificationFailure(notification);
                }
            }
        } catch (Exception e) {
            logger.error("Error in processPendingNotifications: {}", e.getMessage(), e);
        }
    }

    /**
     * Processes a single notification by sending it via FCM.
     */
    private void processNotification(Notification notification) {
        // Check retry count
        if (notification.getRetryCount() != null && notification.getRetryCount() >= MAX_RETRY_COUNT) {
            notification.setStatus("FAILED");
            notificationRepository.save(notification);
            logger.warn("Notification {} exceeded max retry count, marking as FAILED", notification.getId());
            return;
        }

        // Get device tokens for the user
        List<DeviceToken> deviceTokens = deviceTokenRepository
            .findByUserIdAndIsActiveTrue(notification.getUser().getId());

        if (deviceTokens.isEmpty()) {
            // No device tokens yet - schedule retry with exponential backoff
            // User might register device token later
            int currentRetryCount = notification.getRetryCount() != null ? notification.getRetryCount() : 0;
            int newRetryCount = currentRetryCount + 1;
            notification.setRetryCount(newRetryCount);
            
            // Calculate next retry time using exponential backoff
            if (newRetryCount < RETRY_DELAYS_MINUTES.length) {
                int delayMinutes = RETRY_DELAYS_MINUTES[newRetryCount];
                notification.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMinutes));
                logger.info("No device tokens found for user {}. Notification {} will retry in {} minutes (attempt {})",
                    notification.getUser().getId(), notification.getId(), delayMinutes, newRetryCount);
            } else {
                // Exceeded max retries - mark as failed
                notification.setStatus("FAILED");
                notification.setNextRetryAt(null);
                logger.warn("No device tokens found for user {} after {} attempts. Notification {} marked as FAILED.",
                    notification.getUser().getId(), MAX_RETRY_COUNT, notification.getId());
            }
            notificationRepository.save(notification);
            return;
        }

        // Build FCM notification
        FCMNotificationDTO fcmNotification = FCMNotificationDTO.builder()
            .title(notification.getTitle())
            .body(notification.getBody())
            .data(notification.getData())
            .build();

        // Check if FirebaseMessagingService is available
        if (firebaseMessagingService == null) {
            logger.error("FirebaseMessagingService is not available. Cannot send notification {}.", notification.getId());
            handleNotificationFailure(notification);
            return;
        }
        
        // Send to all device tokens
        boolean sent = false;
        int successCount = 0;
        int failureCount = 0;
        
        for (DeviceToken deviceToken : deviceTokens) {
            try {
                String result = firebaseMessagingService
                    .sendNotificationToDevice(deviceToken.getDeviceToken(), fcmNotification);
                
                // Check if result indicates success
                if (result != null && !result.startsWith("INVALID") && 
                    !result.startsWith("FAILED") && !result.startsWith("UNAVAILABLE")) {
                    sent = true;
                    successCount++;
                    logger.debug("Notification {} sent successfully to device {}", 
                        notification.getId(), deviceToken.getId());
                } else {
                    failureCount++;
                    logger.warn("Notification {} failed to send to device {}: {}", 
                        notification.getId(), deviceToken.getId(), result);
                    
                    // If token is invalid, mark device token as inactive
                    if (result != null && result.startsWith("INVALID")) {
                        logger.info("Marking device token {} as inactive due to invalid token", deviceToken.getId());
                        deviceToken.setIsActive(false);
                        deviceTokenRepository.save(deviceToken);
                    }
                }
            } catch (IllegalStateException e) {
                // Firebase not initialized
                logger.error("Firebase not initialized. Cannot send notification {}: {}", 
                    notification.getId(), e.getMessage());
                failureCount++;
            } catch (Exception e) {
                failureCount++;
                logger.error("Unexpected error sending notification {} to device {}: {}", 
                    notification.getId(), deviceToken.getId(), e.getMessage(), e);
            }
        }
        
        logger.info("Notification {} sending completed: {} success, {} failures out of {} devices", 
            notification.getId(), successCount, failureCount, deviceTokens.size());

        // Update notification status
        if (sent) {
            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());
            notification.setNextRetryAt(null); // Clear next retry time
            logger.info("Notification {} sent successfully via database queue processor.", notification.getId());
        } else {
            // Increment retry count and schedule next retry with exponential backoff
            int currentRetryCount = notification.getRetryCount() != null ? notification.getRetryCount() : 0;
            int newRetryCount = currentRetryCount + 1;
            notification.setRetryCount(newRetryCount);
            
            // Calculate next retry time using exponential backoff
            if (newRetryCount < RETRY_DELAYS_MINUTES.length) {
                int delayMinutes = RETRY_DELAYS_MINUTES[newRetryCount];
                notification.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMinutes));
                logger.warn("Notification {} failed to send, retry count: {}, next retry in {} minutes", 
                    notification.getId(), newRetryCount, delayMinutes);
            } else {
                // Exceeded max retries
                notification.setStatus("FAILED");
                notification.setNextRetryAt(null);
                logger.warn("Notification {} exceeded max retry count, marking as FAILED", notification.getId());
            }
        }

        notificationRepository.save(notification);
    }

    /**
     * Handles notification processing failure.
     */
    private void handleNotificationFailure(Notification notification) {
        int currentRetryCount = notification.getRetryCount() != null ? notification.getRetryCount() : 0;
        int newRetryCount = currentRetryCount + 1;
        
        if (newRetryCount >= MAX_RETRY_COUNT) {
            notification.setStatus("FAILED");
            notification.setNextRetryAt(null);
            logger.warn("Notification {} exceeded max retry count, marking as FAILED", notification.getId());
        } else {
            notification.setRetryCount(newRetryCount);
            // Calculate next retry time using exponential backoff
            if (newRetryCount < RETRY_DELAYS_MINUTES.length) {
                int delayMinutes = RETRY_DELAYS_MINUTES[newRetryCount];
                notification.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMinutes));
                logger.warn("Notification {} failed, retry count: {}, next retry in {} minutes", 
                    notification.getId(), newRetryCount, delayMinutes);
            }
            // Keep as PENDING for retry
        }
        
        notificationRepository.save(notification);
    }
}

