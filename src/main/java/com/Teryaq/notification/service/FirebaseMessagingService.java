package com.Teryaq.notification.service;

import com.Teryaq.notification.dto.FCMNotificationDTO;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FirebaseMessagingService {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebaseMessagingService.class);
    
    @Autowired(required = false)
    private FirebaseMessaging firebaseMessaging;
    
    /**
     * إرسال إشعار لجهاز واحد
     */
    public String sendNotificationToDevice(String deviceToken, FCMNotificationDTO notificationDTO) {
        if (firebaseMessaging == null) {
            logger.warn("Firebase Messaging is not initialized");
            return null;
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
            logger.info("Successfully sent notification to device: {}", deviceToken);
            return response;
        } catch (FirebaseMessagingException e) {
            logger.error("Error sending notification to device {}: {}", deviceToken, e.getMessage());
            return null;
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
            logger.warn("Firebase Messaging is not initialized");
            deviceTokens.forEach(token -> results.put(token, "FAILED: Firebase not initialized"));
            return results;
        }
        
        for (String deviceToken : deviceTokens) {
            String result = sendNotificationToDevice(deviceToken, notificationDTO);
            results.put(deviceToken, result != null ? "SUCCESS" : "FAILED");
        }
        
        return results;
    }
    
    /**
     * إرسال إشعار لموضوع (Topic)
     */
    public String sendNotificationToTopic(String topic, FCMNotificationDTO notificationDTO) {
        if (firebaseMessaging == null) {
            logger.warn("Firebase Messaging is not initialized");
            return null;
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
            logger.info("Successfully sent notification to topic: {}", topic);
            return response;
        } catch (FirebaseMessagingException e) {
            logger.error("Error sending notification to topic {}: {}", topic, e.getMessage());
            return null;
        }
    }
}


