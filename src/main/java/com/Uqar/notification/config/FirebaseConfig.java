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
            logger.info("Firebase Messaging is disabled via configuration");
            return;
        }
        
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                String resourcePath = credentialsPath.replace("classpath:", "");
                logger.info("Attempting to load Firebase credentials from: {}", resourcePath);
                
                InputStream serviceAccount = getClass().getClassLoader()
                    .getResourceAsStream(resourcePath);
                
                if (serviceAccount == null) {
                    logger.error("❌ Firebase credentials file not found at: {} (resolved from: {})", 
                        resourcePath, credentialsPath);
                    logger.error("Please ensure the file exists at: src/main/resources/{}", resourcePath);
                    throw new IllegalStateException(
                        "Firebase credentials file not found at: " + resourcePath + ". " +
                        "Please ensure the file exists at src/main/resources/" + resourcePath
                    );
                }
                
                logger.info("Firebase credentials file found, initializing Firebase...");
                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId(projectId)
                    .build();
                
                FirebaseApp.initializeApp(options);
                logger.info("✅ Firebase initialized successfully with project ID: {}", projectId);
            } else {
                FirebaseApp existingApp = FirebaseApp.getInstance();
                logger.info("Firebase already initialized: {}", existingApp.getName());
            }
        } catch (IOException e) {
            logger.error("❌ IOException while initializing Firebase: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to initialize Firebase due to IO error: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("❌ Unexpected error initializing Firebase: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to initialize Firebase: " + e.getMessage(), e);
        }
    }
    
    @Bean
    @ConditionalOnProperty(name = "firebase.messaging.enabled", havingValue = "true", matchIfMissing = true)
    public FirebaseApp firebaseApp() {
        if (FirebaseApp.getApps().isEmpty()) {
            logger.error("FirebaseApp is not initialized. " +
                        "This usually means Firebase initialization failed during @PostConstruct. " +
                        "Check logs above for initialization errors.");
            // Don't return null - throw exception to prevent bean creation
            throw new IllegalStateException(
                "FirebaseApp is not initialized. " +
                "Please check: 1) Firebase credentials file exists at the configured path, " +
                "2) Credentials file is valid JSON, 3) Project ID is correct, " +
                "4) Check @PostConstruct initialization logs for errors"
            );
        }
        FirebaseApp app = FirebaseApp.getInstance();
        logger.info("FirebaseApp bean created successfully: {}", app.getName());
        return app;
    }
    
    /**
     * Creates FirebaseMessaging bean from FirebaseApp
     * This bean is required for sending notifications
     * 
     * Note: This method will only create the bean if FirebaseApp is properly initialized.
     * If FirebaseApp is null or initialization failed, this bean will not be created,
     * which will prevent FirebaseMessagingService from being created (due to @ConditionalOnBean).
     */
    @Bean
    @ConditionalOnProperty(name = "firebase.messaging.enabled", havingValue = "true", matchIfMissing = true)
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        if (firebaseApp == null) {
            logger.error("FirebaseApp is null. Cannot create FirebaseMessaging bean. " +
                        "Check Firebase initialization logs above for errors.");
            // Don't return null - throw exception to prevent bean creation
            throw new IllegalStateException(
                "FirebaseApp is not initialized. " +
                "Please check: 1) Firebase credentials file exists, 2) Firebase initialization succeeded, " +
                "3) firebase.messaging.enabled=true in application.yml"
            );
        }
        
        try {
            FirebaseMessaging messaging = FirebaseMessaging.getInstance(firebaseApp);
            logger.info("FirebaseMessaging bean created successfully");
            return messaging;
        } catch (Exception e) {
            logger.error("Error creating FirebaseMessaging bean: {}", e.getMessage(), e);
            // Don't return null - throw exception to prevent bean creation
            throw new IllegalStateException("Failed to create FirebaseMessaging bean: " + e.getMessage(), e);
        }
    }
}


