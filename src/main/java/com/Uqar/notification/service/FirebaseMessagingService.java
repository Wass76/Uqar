package com.Uqar.notification.service;

import com.Uqar.notification.dto.FCMNotificationDTO;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@ConditionalOnBean(FirebaseMessaging.class)
public class FirebaseMessagingService {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebaseMessagingService.class);
    private static final int MAX_MULTICAST_TOKENS = 500; // Firebase limit for multicast
    
    private final FirebaseMessaging firebaseMessaging;
    private final RateLimiter firebaseRateLimiter;
    private final MeterRegistry meterRegistry;
    
    // Metrics
    private final Counter notificationsSentSuccess;
    private final Counter notificationsSentFailed;
    private final Counter notificationsSentInvalidToken;
    private final Counter notificationsSentUnavailable;
    private final Timer notificationSendDuration;
    
    @Autowired
    public FirebaseMessagingService(FirebaseMessaging firebaseMessaging,
                                   RateLimiterRegistry rateLimiterRegistry,
                                   MeterRegistry meterRegistry) {
        if (firebaseMessaging == null) {
            throw new IllegalStateException("FirebaseMessaging bean cannot be null. " +
                    "This service should only be created when FirebaseMessaging bean exists.");
        }
        this.firebaseMessaging = firebaseMessaging;
        this.meterRegistry = meterRegistry;
        
        // Initialize rate limiter for Firebase (900 requests per minute to stay under 1000/sec limit)
        this.firebaseRateLimiter = rateLimiterRegistry.rateLimiter("firebase", () -> 
            io.github.resilience4j.ratelimiter.RateLimiterConfig.custom()
                .limitForPeriod(900) // 900 requests per period
                .limitRefreshPeriod(java.time.Duration.ofMinutes(1))
                .timeoutDuration(java.time.Duration.ofSeconds(5))
                .build()
        );
        
        // Initialize metrics
        this.notificationsSentSuccess = Counter.builder("firebase.notifications.sent")
            .tag("status", "success")
            .description("Number of successfully sent Firebase notifications")
            .register(meterRegistry);
        
        this.notificationsSentFailed = Counter.builder("firebase.notifications.sent")
            .tag("status", "failed")
            .description("Number of failed Firebase notifications")
            .register(meterRegistry);
        
        this.notificationsSentInvalidToken = Counter.builder("firebase.notifications.sent")
            .tag("status", "invalid_token")
            .description("Number of notifications failed due to invalid token")
            .register(meterRegistry);
        
        this.notificationsSentUnavailable = Counter.builder("firebase.notifications.sent")
            .tag("status", "unavailable")
            .description("Number of notifications failed due to Firebase unavailability")
            .register(meterRegistry);
        
        this.notificationSendDuration = Timer.builder("firebase.notifications.duration")
            .description("Time taken to send Firebase notifications")
            .register(meterRegistry);
        
        logger.info("FirebaseMessagingService initialized successfully with FirebaseMessaging bean, rate limiter, and metrics");
    }
    
    /**
     * إرسال إشعار لجهاز واحد
     */
    public String sendNotificationToDevice(String deviceToken, FCMNotificationDTO notificationDTO) {
        if (firebaseMessaging == null) {
            logger.error("Firebase Messaging is not initialized. Cannot send notification.");
            throw new IllegalStateException("Firebase Messaging is not initialized");
        }
        
        if (deviceToken == null || deviceToken.trim().isEmpty()) {
            logger.warn("Device token is null or empty. Cannot send notification.");
            notificationsSentInvalidToken.increment();
            return "INVALID_TOKEN";
        }
        
        // Use rate limiter and metrics
        return firebaseRateLimiter.executeSupplier(() -> {
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                Notification notification = Notification.builder()
                    .setTitle(notificationDTO.getTitle())
                    .setBody(notificationDTO.getBody())
                    .setImage(notificationDTO.getImageUrl())
                    .build();
                
                Message.Builder messageBuilder = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(notification);
                
                if (notificationDTO.getData() != null && !notificationDTO.getData().isEmpty()) {
                    Map<String, String> dataMap = notificationDTO.getData().entrySet().stream()
                        .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().toString()
                        ));
                    messageBuilder.putAllData(dataMap);
                }
                
                if (notificationDTO.getClickAction() != null) {
                    messageBuilder.putData("click_action", notificationDTO.getClickAction());
                }
                
                Message message = messageBuilder.build();
                String response = firebaseMessaging.send(message);
                logger.info("Successfully sent notification to device: {} | Response: {}", deviceToken, response);
                notificationsSentSuccess.increment();
                return response;
            } catch (FirebaseMessagingException e) {
                logger.error("FirebaseMessagingException sending notification to device {}: {} | Error code: {}", 
                    deviceToken, e.getMessage(), e.getErrorCode(), e);
                
                // Handle specific Firebase errors
                if (e.getErrorCode() != null) {
                    String errorCode = e.getErrorCode().toString();
                    if (errorCode.contains("INVALID_ARGUMENT") || 
                        errorCode.contains("INVALID_REGISTRATION_TOKEN") ||
                        errorCode.contains("REGISTRATION_TOKEN_NOT_REGISTERED")) {
                        logger.warn("Invalid or unregistered device token: {}", deviceToken);
                        notificationsSentInvalidToken.increment();
                        return "INVALID_TOKEN";
                    } else if (errorCode.contains("UNAVAILABLE") || errorCode.contains("INTERNAL")) {
                        logger.warn("Firebase service unavailable, will retry: {}", e.getMessage());
                        notificationsSentUnavailable.increment();
                        return "UNAVAILABLE";
                    }
                }
                notificationsSentFailed.increment();
                return "FAILED";
            } catch (Exception e) {
                logger.error("Unexpected error sending notification to device {}: {}", deviceToken, e.getMessage(), e);
                notificationsSentFailed.increment();
                return "FAILED";
            } finally {
                sample.stop(notificationSendDuration);
            }
        });
    }
    
    /**
     * إرسال إشعار لعدة أجهزة (Multicast) - Uses Firebase Batch API for efficiency
     */
    public Map<String, String> sendNotificationToMultipleDevices(
            List<String> deviceTokens, 
            FCMNotificationDTO notificationDTO) {
        
        Map<String, String> results = new HashMap<>();
        
        if (firebaseMessaging == null) {
            logger.error("Firebase Messaging is not initialized");
            if (deviceTokens != null) {
                deviceTokens.forEach(token -> results.put(token, "FAILED: Firebase not initialized"));
            }
            return results;
        }
        
        if (deviceTokens == null || deviceTokens.isEmpty()) {
            logger.warn("No device tokens provided for multicast notification");
            return results;
        }
        
        // Remove null/empty tokens
        List<String> validTokens = deviceTokens.stream()
            .filter(token -> token != null && !token.trim().isEmpty())
            .collect(Collectors.toList());
        
        if (validTokens.isEmpty()) {
            logger.warn("No valid device tokens provided for multicast notification");
            deviceTokens.forEach(token -> results.put(token, "INVALID_TOKEN"));
            return results;
        }
        
        logger.info("Sending multicast notification to {} devices using Firebase Batch API", validTokens.size());
        
        // Use rate limiter for the entire batch operation
        return firebaseRateLimiter.executeSupplier(() -> {
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                // Build notification
                Notification notification = Notification.builder()
                    .setTitle(notificationDTO.getTitle())
                    .setBody(notificationDTO.getBody())
                    .setImage(notificationDTO.getImageUrl())
                    .build();
                
                // Build data map
                Map<String, String> dataMap = new HashMap<>();
                if (notificationDTO.getData() != null && !notificationDTO.getData().isEmpty()) {
                    dataMap = notificationDTO.getData().entrySet().stream()
                        .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().toString()
                        ));
                }
                
                if (notificationDTO.getClickAction() != null) {
                    dataMap.put("click_action", notificationDTO.getClickAction());
                }
                
                // Split into batches of 500 (Firebase limit)
                List<List<String>> batches = new ArrayList<>();
                for (int i = 0; i < validTokens.size(); i += MAX_MULTICAST_TOKENS) {
                    int end = Math.min(i + MAX_MULTICAST_TOKENS, validTokens.size());
                    batches.add(validTokens.subList(i, end));
                }
                
                logger.debug("Split {} tokens into {} batches", validTokens.size(), batches.size());
                
                // Process each batch
                for (List<String> batch : batches) {
                    try {
                        MulticastMessage multicastMessage = MulticastMessage.builder()
                            .setNotification(notification)
                            .putAllData(dataMap)
                            .addAllTokens(batch)
                            .build();
                        
                        BatchResponse batchResponse = firebaseMessaging.sendMulticast(multicastMessage);
                        
                        // Process results for this batch
                        List<SendResponse> responses = batchResponse.getResponses();
                        for (int i = 0; i < batch.size() && i < responses.size(); i++) {
                            String token = batch.get(i);
                            SendResponse response = responses.get(i);
                            
                            if (response.isSuccessful()) {
                                results.put(token, "SUCCESS");
                                notificationsSentSuccess.increment();
                            } else {
                                FirebaseMessagingException exception = response.getException();
                                String errorCode = exception != null && exception.getErrorCode() != null 
                                    ? exception.getErrorCode().toString() 
                                    : "UNKNOWN";
                                
                                if (errorCode.contains("INVALID_ARGUMENT") || 
                                    errorCode.contains("INVALID_REGISTRATION_TOKEN") ||
                                    errorCode.contains("REGISTRATION_TOKEN_NOT_REGISTERED")) {
                                    results.put(token, "INVALID_TOKEN");
                                    notificationsSentInvalidToken.increment();
                                } else if (errorCode.contains("UNAVAILABLE") || errorCode.contains("INTERNAL")) {
                                    results.put(token, "UNAVAILABLE");
                                    notificationsSentUnavailable.increment();
                                } else {
                                    results.put(token, "FAILED");
                                    notificationsSentFailed.increment();
                                }
                                
                                logger.debug("Failed to send to token {}: {}", token, 
                                    exception != null ? exception.getMessage() : "Unknown error");
                            }
                        }
                        
                        logger.info("Batch completed: {}/{} successful", 
                            batchResponse.getSuccessCount(), batch.size());
                            
                    } catch (Exception e) {
                        logger.error("Error sending batch: {}", e.getMessage(), e);
                        // Mark all tokens in this batch as failed
                        batch.forEach(token -> {
                            results.put(token, "FAILED: " + e.getMessage());
                            notificationsSentFailed.increment();
                        });
                    }
                }
                
                // Handle any tokens that weren't in validTokens list
                deviceTokens.stream()
                    .filter(token -> token == null || token.trim().isEmpty())
                    .forEach(token -> {
                        results.put(token, "INVALID_TOKEN");
                        notificationsSentInvalidToken.increment();
                    });
                
                long successCount = results.values().stream().filter(v -> "SUCCESS".equals(v)).count();
                logger.info("Multicast notification completed: {}/{} successful", successCount, deviceTokens.size());
                
                return results;
            } catch (Exception e) {
                logger.error("Unexpected error in multicast notification: {}", e.getMessage(), e);
                // Mark all tokens as failed
                validTokens.forEach(token -> {
                    results.put(token, "FAILED: " + e.getMessage());
                    notificationsSentFailed.increment();
                });
                return results;
            } finally {
                sample.stop(notificationSendDuration);
            }
        });
    }
    
    /**
     * إرسال إشعار لموضوع (Topic)
     */
    public String sendNotificationToTopic(String topic, FCMNotificationDTO notificationDTO) {
        if (firebaseMessaging == null) {
            logger.error("Firebase Messaging is not initialized. Cannot send topic notification.");
            throw new IllegalStateException("Firebase Messaging is not initialized");
        }
        
        if (topic == null || topic.trim().isEmpty()) {
            logger.warn("Topic is null or empty. Cannot send notification.");
            return "INVALID_TOPIC";
        }
        
        try {
            Notification notification = Notification.builder()
                .setTitle(notificationDTO.getTitle())
                .setBody(notificationDTO.getBody())
                .setImage(notificationDTO.getImageUrl())
                .build();
            
            Message.Builder messageBuilder = Message.builder()
                .setTopic(topic)
                .setNotification(notification);
            
            if (notificationDTO.getData() != null && !notificationDTO.getData().isEmpty()) {
                Map<String, String> dataMap = notificationDTO.getData().entrySet().stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().toString()
                    ));
                messageBuilder.putAllData(dataMap);
            }
            
            Message message = messageBuilder.build();
            String response = firebaseMessaging.send(message);
            logger.info("Successfully sent notification to topic: {} | Response: {}", topic, response);
            return response;
        } catch (FirebaseMessagingException e) {
            logger.error("FirebaseMessagingException sending notification to topic {}: {} | Error code: {}", 
                topic, e.getMessage(), e.getErrorCode(), e);
            return "FAILED";
        } catch (Exception e) {
            logger.error("Unexpected error sending notification to topic {}: {}", topic, e.getMessage(), e);
            return "FAILED";
        }
    }
}


