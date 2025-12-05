package com.Uqar.notification.service;

import com.Uqar.notification.dto.FCMNotificationDTO;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@ConditionalOnBean(FirebaseMessaging.class)
public class FirebaseMessagingService {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebaseMessagingService.class);
    
    private final FirebaseMessaging firebaseMessaging;
    
    @Autowired
    public FirebaseMessagingService(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
        logger.info("FirebaseMessagingService initialized with FirebaseMessaging bean");
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
            return "INVALID_TOKEN";
        }
        
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
                    return "INVALID_TOKEN";
                } else if (errorCode.contains("UNAVAILABLE") || errorCode.contains("INTERNAL")) {
                    logger.warn("Firebase service unavailable, will retry: {}", e.getMessage());
                    return "UNAVAILABLE";
                }
            }
            return "FAILED";
        } catch (Exception e) {
            logger.error("Unexpected error sending notification to device {}: {}", deviceToken, e.getMessage(), e);
            return "FAILED";
        }
    }
    
    /**
     * إرسال إشعار لعدة أجهزة (Multicast)
     */
    public Map<String, String> sendNotificationToMultipleDevices(
            List<String> deviceTokens, 
            FCMNotificationDTO notificationDTO) {
        
        Map<String, String> results = new HashMap<>();
        
        if (firebaseMessaging == null) {
            logger.error("Firebase Messaging is not initialized");
            deviceTokens.forEach(token -> results.put(token, "FAILED: Firebase not initialized"));
            return results;
        }
        
        if (deviceTokens == null || deviceTokens.isEmpty()) {
            logger.warn("No device tokens provided for multicast notification");
            return results;
        }
        
        logger.info("Sending multicast notification to {} devices", deviceTokens.size());
        
        for (String deviceToken : deviceTokens) {
            try {
                String result = sendNotificationToDevice(deviceToken, notificationDTO);
                // Check if result indicates success (not null and not error codes)
                if (result != null && !result.startsWith("INVALID") && 
                    !result.startsWith("FAILED") && !result.startsWith("UNAVAILABLE")) {
                    results.put(deviceToken, "SUCCESS");
                } else {
                    results.put(deviceToken, result != null ? result : "FAILED");
                }
            } catch (Exception e) {
                logger.error("Error sending to device {}: {}", deviceToken, e.getMessage());
                results.put(deviceToken, "FAILED: " + e.getMessage());
            }
        }
        
        long successCount = results.values().stream().filter(v -> "SUCCESS".equals(v)).count();
        logger.info("Multicast notification completed: {}/{} successful", successCount, deviceTokens.size());
        
        return results;
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


