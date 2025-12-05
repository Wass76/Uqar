package com.Uqar.notification.config;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import jakarta.annotation.PostConstruct;

@Configuration
public class FirebaseConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);
    
    @Value("${firebase.credentials.path}")
    private String credentialsPath;
    
    @Value("${firebase.project-id}")
    private String projectId;
    
    @Value("${firebase.messaging.enabled:true}")
    private boolean messagingEnabled;
    
    @PostConstruct
    public void initialize() {
        if (!messagingEnabled) {
            logger.info("Firebase Messaging is disabled");
            return;
        }
        
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = getClass().getClassLoader()
                    .getResourceAsStream(credentialsPath.replace("classpath:", ""));
                
                if (serviceAccount == null) {
                    logger.error("Firebase credentials file not found at: {}", credentialsPath);
                    return;
                }
                
                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId(projectId)
                    .build();
                
                FirebaseApp.initializeApp(options);
                logger.info("Firebase initialized successfully");
            } else {
                logger.info("Firebase already initialized");
            }
        } catch (IOException e) {
            logger.error("Error initializing Firebase: {}", e.getMessage(), e);
        }
    }
    
    @Bean
    @ConditionalOnProperty(name = "firebase.messaging.enabled", havingValue = "true", matchIfMissing = true)
    public FirebaseApp firebaseApp() {
        if (FirebaseApp.getApps().isEmpty()) {
            logger.warn("FirebaseApp is not initialized. FirebaseMessaging bean will not be created.");
            return null;
        }
        return FirebaseApp.getInstance();
    }
    
    /**
     * Creates FirebaseMessaging bean from FirebaseApp
     * This bean is required for sending notifications
     */
    @Bean
    @ConditionalOnProperty(name = "firebase.messaging.enabled", havingValue = "true", matchIfMissing = true)
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        if (firebaseApp == null) {
            logger.warn("FirebaseApp is null. FirebaseMessaging bean will not be created.");
            return null;
        }
        
        try {
            FirebaseMessaging messaging = FirebaseMessaging.getInstance(firebaseApp);
            logger.info("FirebaseMessaging bean created successfully");
            return messaging;
        } catch (Exception e) {
            logger.error("Error creating FirebaseMessaging bean: {}", e.getMessage(), e);
            return null;
        }
    }
}


