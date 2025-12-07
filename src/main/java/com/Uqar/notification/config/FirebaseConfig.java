package com.Uqar.notification.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.Uqar.notification.service.FirebaseMessagingService;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.micrometer.core.instrument.MeterRegistry;

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
    
    @Value("${firebase.credentials.path:classpath:firebase/serviceAccountKey.json}")
    private String credentialsPath;
    
    @Value("${firebase.project-id:}")
    private String projectId;
    
    @Value("${firebase.messaging.enabled:true}")
    private boolean messagingEnabled;
    
    // Track initialization status
    private static boolean initializationFailed = false;
    
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
            logger.info("Application will continue without Firebase notifications");
            logger.info("═══════════════════════════════════════════════════════════");
            initializationFailed = false; // Not a failure, just disabled
            return;
        }
        
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = null;
                
                // Check if path is an environment variable or file path
                if (credentialsPath.startsWith("classpath:")) {
                    // Load from classpath
                    String resourcePath = credentialsPath.replace("classpath:", "");
                    logger.info("Attempting to load Firebase credentials from classpath: {}", resourcePath);
                    
                    ClassLoader classLoader = getClass().getClassLoader();
                    logger.info("Using ClassLoader: {}", classLoader.getClass().getName());
                    
                    serviceAccount = classLoader.getResourceAsStream(resourcePath);
                    
                    if (serviceAccount == null) {
                        // Try alternative paths
                        logger.warn("Resource not found at: {}, trying alternative paths...", resourcePath);
                        serviceAccount = classLoader.getResourceAsStream("/" + resourcePath);
                        if (serviceAccount == null) {
                            logger.warn("Trying: firebase/serviceAccountKey.json");
                            serviceAccount = classLoader.getResourceAsStream("firebase/serviceAccountKey.json");
                        }
                    }
                } else {
                    // Load from file system (environment variable or absolute path)
                    logger.info("Attempting to load Firebase credentials from file system: {}", credentialsPath);
                    try {
                        java.io.File file = new java.io.File(credentialsPath);
                        if (file.exists() && file.isFile()) {
                            serviceAccount = new java.io.FileInputStream(file);
                            logger.info("✅ Firebase credentials file found at: {}", credentialsPath);
                        } else {
                            logger.warn("File not found at: {}", credentialsPath);
                        }
                    } catch (Exception e) {
                        logger.warn("Error reading file from path {}: {}", credentialsPath, e.getMessage());
                    }
                }
                
                if (serviceAccount == null) {
                    logger.error("═══════════════════════════════════════════════════════════");
                    logger.error("❌ WARNING: Firebase credentials file NOT FOUND");
                    logger.error("═══════════════════════════════════════════════════════════");
                    logger.error("Tried paths:");
                    if (credentialsPath.startsWith("classpath:")) {
                        String resourcePath = credentialsPath.replace("classpath:", "");
                        logger.error("  1. {}", resourcePath);
                        logger.error("  2. /{}", resourcePath);
                        logger.error("  3. firebase/serviceAccountKey.json");
                    } else {
                        logger.error("  1. {}", credentialsPath);
                    }
                    logger.error("");
                    logger.error("⚠️  APPLICATION WILL CONTINUE WITHOUT FIREBASE NOTIFICATIONS");
                    logger.error("   Set firebase.messaging.enabled=false to suppress this warning");
                    logger.error("   Or provide credentials via:");
                    logger.error("   - Environment variable: FIREBASE_CREDENTIALS_PATH");
                    logger.error("   - Docker secret mount");
                    logger.error("   - File path in application.yml");
                    logger.error("═══════════════════════════════════════════════════════════");
                    
                    initializationFailed = true;
                    return; // Don't throw exception - allow app to continue
                }
                
                logger.info("✅ Firebase credentials file FOUND");
                
                // Read the file to verify it's valid
                byte[] bytes = serviceAccount.readAllBytes();
                serviceAccount.close();
                logger.info("Credentials file size: {} bytes", bytes.length);
                
                if (bytes.length == 0) {
                    logger.error("Firebase credentials file is empty!");
                    initializationFailed = true;
                    return; // Don't throw - allow app to continue
                }
                
                // Verify it's valid JSON
                try {
                    String jsonContent = new String(bytes);
                    if (!jsonContent.trim().startsWith("{")) {
                        logger.error("Firebase credentials file does not appear to be valid JSON");
                        initializationFailed = true;
                        return; // Don't throw - allow app to continue
                    }
                    logger.info("✅ Credentials file appears to be valid JSON");
                } catch (Exception e) {
                    logger.warn("Could not verify JSON format: {}", e.getMessage());
                }
                
                // Create new InputStream from bytes for Firebase
                java.io.ByteArrayInputStream credentialsStream = new java.io.ByteArrayInputStream(bytes);
                
                logger.info("Creating FirebaseOptions...");
                FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream));
                
                if (projectId != null && !projectId.trim().isEmpty()) {
                    optionsBuilder.setProjectId(projectId);
                }
                
                FirebaseOptions options = optionsBuilder.build();
                
                logger.info("Initializing FirebaseApp...");
                FirebaseApp.initializeApp(options);
                initializationFailed = false;
                logger.info("═══════════════════════════════════════════════════════════");
                logger.info("✅ Firebase initialized SUCCESSFULLY");
                logger.info("   Project ID: {}", projectId);
                logger.info("   App Name: {}", FirebaseApp.getInstance().getName());
                logger.info("═══════════════════════════════════════════════════════════");
            } else {
                FirebaseApp existingApp = FirebaseApp.getInstance();
                logger.info("Firebase already initialized: {}", existingApp.getName());
                initializationFailed = false;
            }
        } catch (IOException e) {
            logger.error("═══════════════════════════════════════════════════════════");
            logger.error("❌ IOException while initializing Firebase");
            logger.error("═══════════════════════════════════════════════════════════");
            logger.error("Error: {}", e.getMessage());
            logger.error("⚠️  APPLICATION WILL CONTINUE WITHOUT FIREBASE NOTIFICATIONS");
            initializationFailed = true;
            // Don't throw exception - allow app to continue
        } catch (IllegalArgumentException e) {
            logger.error("═══════════════════════════════════════════════════════════");
            logger.error("❌ Invalid Firebase credentials");
            logger.error("═══════════════════════════════════════════════════════════");
            logger.error("Error: {}", e.getMessage());
            logger.error("This usually means:");
            logger.error("  - Credentials file is invalid or corrupted");
            logger.error("  - JSON format is malformed");
            logger.error("  - Required fields are missing");
            logger.error("⚠️  APPLICATION WILL CONTINUE WITHOUT FIREBASE NOTIFICATIONS");
            initializationFailed = true;
            // Don't throw exception - allow app to continue
        } catch (Exception e) {
            logger.error("═══════════════════════════════════════════════════════════");
            logger.error("❌ Unexpected error initializing Firebase");
            logger.error("═══════════════════════════════════════════════════════════");
            logger.error("Exception type: {}", e.getClass().getName());
            logger.error("Error: {}", e.getMessage());
            logger.error("⚠️  APPLICATION WILL CONTINUE WITHOUT FIREBASE NOTIFICATIONS");
            initializationFailed = true;
            // Don't throw exception - allow app to continue
        }
    }
    
    /**
     * Check if Firebase initialization failed
     */
    public static boolean isInitializationFailed() {
        return initializationFailed;
    }
    
    @Bean
    @ConditionalOnProperty(name = "firebase.messaging.enabled", havingValue = "true", matchIfMissing = true)
    public FirebaseApp firebaseApp() {
        try {
            // Check if initialization failed
            if (initializationFailed) {
                logger.warn("FirebaseApp initialization failed previously. Bean will not be created.");
                return null;
            }
            
            // Try to get FirebaseApp instance
            List<FirebaseApp> apps = FirebaseApp.getApps();
            logger.debug("Found {} FirebaseApp instances", apps.size());
            
            if (apps.isEmpty()) {
                logger.warn("FirebaseApp is not initialized. " +
                           "This usually means Firebase initialization failed during @PostConstruct. " +
                           "Check logs above for initialization errors.");
                return null;
            }
            
            // Get the default app (or first app)
            FirebaseApp app;
            try {
                app = FirebaseApp.getInstance();
            } catch (IllegalStateException e) {
                // If no default app, try to get the first one
                if (!apps.isEmpty()) {
                    app = apps.get(0);
                    logger.info("Using first available FirebaseApp: {}", app.getName());
                } else {
                    logger.error("No FirebaseApp instances available");
                    return null;
                }
            }
            
            logger.info("═══════════════════════════════════════════════════════════");
            logger.info("✅ FirebaseApp bean created successfully: {}", app.getName());
            logger.info("═══════════════════════════════════════════════════════════");
            return app;
        } catch (Exception e) {
            logger.error("Error creating FirebaseApp bean: {}", e.getMessage(), e);
            return null;
        }
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
    @ConditionalOnBean(FirebaseApp.class)
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        if (firebaseApp == null) {
            logger.error("FirebaseApp is null. Cannot create FirebaseMessaging bean. " +
                       "This should not happen if @ConditionalOnBean is working correctly.");
            return null;
        }
        
        try {
            FirebaseMessaging messaging = FirebaseMessaging.getInstance(firebaseApp);
            logger.info("═══════════════════════════════════════════════════════════");
            logger.info("✅ FirebaseMessaging bean created successfully");
            logger.info("═══════════════════════════════════════════════════════════");
            return messaging;
        } catch (Exception e) {
            logger.error("═══════════════════════════════════════════════════════════");
            logger.error("❌ Error creating FirebaseMessaging bean: {}", e.getMessage(), e);
            logger.error("═══════════════════════════════════════════════════════════");
            logger.warn("Application will continue without Firebase notifications");
            return null;
        }
    }
    
    /**
     * Creates FirebaseMessagingService bean.
     * This service is only created when FirebaseMessaging bean exists.
     * Created as @Bean method to ensure proper creation order.
     */
    @Bean
    @ConditionalOnProperty(name = "firebase.messaging.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnBean(FirebaseMessaging.class)
    public FirebaseMessagingService firebaseMessagingService(
            FirebaseMessaging firebaseMessaging,
            RateLimiterRegistry rateLimiterRegistry,
            MeterRegistry meterRegistry) {
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("🔥 Creating FirebaseMessagingService bean");
        logger.info("═══════════════════════════════════════════════════════════");
        
        try {
            FirebaseMessagingService service = new FirebaseMessagingService(
                firebaseMessaging,
                rateLimiterRegistry,
                meterRegistry
            );
            logger.info("✅ FirebaseMessagingService bean created successfully");
            return service;
        } catch (Exception e) {
            logger.error("❌ Error creating FirebaseMessagingService bean: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to create FirebaseMessagingService", e);
        }
    }
}


