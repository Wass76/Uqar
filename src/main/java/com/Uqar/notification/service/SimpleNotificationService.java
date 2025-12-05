package com.Uqar.notification.service;

import com.Uqar.notification.dto.FCMNotificationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple notification service for when Firebase is disabled or unavailable
 * Just logs notifications instead of sending them
 * 
 * This service is created when FirebaseMessagingService is not available
 */
@Service
@ConditionalOnMissingBean(FirebaseMessagingService.class)
public class SimpleNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(SimpleNotificationService.class);
    
    /**
     * Log notification instead of sending
     */
    public String sendNotificationToDevice(String deviceToken, FCMNotificationDTO notificationDTO) {
        logger.info("SIMPLE: Would send notification to device {}: {} - {}", 
                deviceToken, notificationDTO.getTitle(), notificationDTO.getBody());
        return "LOGGED";
    }
    
    /**
     * Log multicast notification instead of sending
     */
    public Map<String, String> sendNotificationToMultipleDevices(
            List<String> deviceTokens, 
            FCMNotificationDTO notificationDTO) {
        
        logger.info("SIMPLE: Would send multicast notification to {} devices: {} - {}", 
                deviceTokens.size(), notificationDTO.getTitle(), notificationDTO.getBody());
        
        Map<String, String> results = new HashMap<>();
        deviceTokens.forEach(token -> results.put(token, "LOGGED"));
        return results;
    }
    
    /**
     * Log topic notification instead of sending
     */
    public String sendNotificationToTopic(String topic, FCMNotificationDTO notificationDTO) {
        logger.info("SIMPLE: Would send notification to topic {}: {} - {}", 
                topic, notificationDTO.getTitle(), notificationDTO.getBody());
        return "LOGGED";
    }
}

