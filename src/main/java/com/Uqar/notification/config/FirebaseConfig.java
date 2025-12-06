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
    
    // Constructor to verify class is being loaded
    public FirebaseConfig() {
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("🔥 FirebaseConfig class is being instantiated");
        logger.info("═══════════════════════════════════════════════════════════");
    }
    
    @Value("${firebase.credentials.path}")
    private String credentialsPath;
    
    @Value("${firebase.project-id}")
    private String projectId;
    
    @Value("${firebase.messaging.enabled:true}")
    private boolean messagingEnabled;
    
    @PostConstruct
    public void initialize() {
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("🔥 Firebase Configuration Initialization Started");
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("Messaging enabled: {}", messagingEnabled);
        logger.info("Credentials path: {}", credentialsPath);
        logger.info("Project ID: {}", projectId);
        
        if (!messagingEnabled) {
            logger.info("Firebase Messaging is disabled via configuration");
            logger.info("═══════════════════════════════════════════════════════════");
            return;
        }
        
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                String resourcePath = credentialsPath.replace("classpath:", "");
                logger.info("Attempting to load Firebase credentials from classpath: {}", resourcePath);
                
                // Try to load the resource
                ClassLoader classLoader = getClass().getClassLoader();
                logger.info("Using ClassLoader: {}", classLoader.getClass().getName());
                
                InputStream serviceAccount = classLoader.getResourceAsStream(resourcePath);
                
                if (serviceAccount == null) {
                    // Try alternative paths
                    logger.warn("Resource not found at: {}, trying alternative paths...", resourcePath);
                    serviceAccount = classLoader.getResourceAsStream("/" + resourcePath);
                    if (serviceAccount == null) {
                        logger.warn("Trying: firebase/serviceAccountKey.json");
                        serviceAccount = classLoader.getResourceAsStream("firebase/serviceAccountKey.json");
                    }
                }
                
                if (serviceAccount == null) {
                    logger.error("═══════════════════════════════════════════════════════════");
                    logger.error("❌ CRITICAL: Firebase credentials file NOT FOUND");
                    logger.error("═══════════════════════════════════════════════════════════");
                    logger.error("Tried paths:");
                    logger.error("  1. {}", resourcePath);
                    logger.error("  2. /{}", resourcePath);
                    logger.error("  3. firebase/serviceAccountKey.json");
                    logger.error("");
                    logger.error("Please verify:");
                    logger.error("  - File exists at: src/main/resources/firebase/serviceAccountKey.json");
                    logger.error("  - File is included in JAR (check target/Uqar-0.0.1-SNAPSHOT.jar)");
                    logger.error("  - File is not excluded in .dockerignore or pom.xml");
                    logger.error("═══════════════════════════════════════════════════════════");
                    
                    // List available resources for debugging
                    try {
                        java.net.URL resourceUrl = classLoader.getResource("firebase");
                        if (resourceUrl != null) {
                            logger.info("Firebase directory found at: {}", resourceUrl);
                        } else {
                            logger.warn("Firebase directory not found in classpath");
                        }
                    } catch (Exception e) {
                        logger.debug("Could not check for firebase directory: {}", e.getMessage());
                    }
                    
                    throw new IllegalStateException(
                        "Firebase credentials file not found. " +
                        "Expected at: " + resourcePath + " in classpath. " +
                        "Please ensure the file exists in src/main/resources/firebase/serviceAccountKey.json " +
                        "and is included in the JAR during Maven build."
                    );
                }
                
                logger.info("✅ Firebase credentials file FOUND");
                
                // Read the file to verify it's valid
                byte[] bytes = serviceAccount.readAllBytes();
                logger.info("Credentials file size: {} bytes", bytes.length);
                
                if (bytes.length == 0) {
                    throw new IllegalStateException("Firebase credentials file is empty!");
                }
                
                // Verify it's valid JSON
                try {
                    String jsonContent = new String(bytes);
                    if (!jsonContent.trim().startsWith("{")) {
                        throw new IllegalStateException("Firebase credentials file does not appear to be valid JSON");
                    }
                    logger.info("✅ Credentials file appears to be valid JSON");
                } catch (Exception e) {
                    logger.warn("Could not verify JSON format: {}", e.getMessage());
                }
                
                // Create new InputStream from bytes for Firebase
                java.io.ByteArrayInputStream credentialsStream = new java.io.ByteArrayInputStream(bytes);
                
                logger.info("Creating FirebaseOptions...");
                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                    .setProjectId(projectId)
                    .build();
                
                logger.info("Initializing FirebaseApp...");
                FirebaseApp.initializeApp(options);
                logger.info("═══════════════════════════════════════════════════════════");
                logger.info("✅ Firebase initialized SUCCESSFULLY");
                logger.info("   Project ID: {}", projectId);
                logger.info("   App Name: {}", FirebaseApp.getInstance().getName());
                logger.info("═══════════════════════════════════════════════════════════");
            } else {
                FirebaseApp existingApp = FirebaseApp.getInstance();
                logger.info("Firebase already initialized: {}", existingApp.getName());
            }
        } catch (IOException e) {
            logger.error("═══════════════════════════════════════════════════════════");
            logger.error("❌ IOException while initializing Firebase");
            logger.error("═══════════════════════════════════════════════════════════");
            logger.error("Error: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to initialize Firebase due to IO error: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            logger.error("═══════════════════════════════════════════════════════════");
            logger.error("❌ Invalid Firebase credentials");
            logger.error("═══════════════════════════════════════════════════════════");
            logger.error("Error: {}", e.getMessage(), e);
            logger.error("This usually means:");
            logger.error("  - Credentials file is invalid or corrupted");
            logger.error("  - JSON format is malformed");
            logger.error("  - Required fields are missing");
            throw new IllegalStateException("Failed to initialize Firebase: Invalid credentials file. " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("═══════════════════════════════════════════════════════════");
            logger.error("❌ Unexpected error initializing Firebase");
            logger.error("═══════════════════════════════════════════════════════════");
            logger.error("Exception type: {}", e.getClass().getName());
            logger.error("Error: {}", e.getMessage(), e);
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


