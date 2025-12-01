package com.Teryaq.notification.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Teryaq.notification.dto.DeviceTokenRequest;
import com.Teryaq.notification.entity.DeviceToken;
import com.Teryaq.notification.repository.DeviceTokenRepository;
import com.Teryaq.user.entity.User;
import com.Teryaq.user.repository.UserRepository;
import com.Teryaq.utils.exception.ResourceNotFoundException;

@Service
@Transactional
public class DeviceTokenService {
    
    private static final Logger logger = LoggerFactory.getLogger(DeviceTokenService.class);
    
    @Autowired
    private DeviceTokenRepository deviceTokenRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * تسجيل device token للمستخدم
     */
    public DeviceToken registerDeviceToken(Long userId, DeviceTokenRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // التحقق من وجود token مشابه
        DeviceToken existingToken = deviceTokenRepository
            .findByDeviceTokenAndIsActiveTrue(request.getDeviceToken())
            .orElse(null);
        
        if (existingToken != null) {
            // إذا كان Token موجود لكن لمستخدم آخر، نعطله
            if (!existingToken.getUser().getId().equals(userId)) {
                existingToken.setIsActive(false);
                deviceTokenRepository.save(existingToken);
            } else {
                // إذا كان نفس المستخدم، نعيد التفعيل
                existingToken.setIsActive(true);
                if (request.getDeviceType() != null) {
                    existingToken.setDeviceType(request.getDeviceType());
                }
                return deviceTokenRepository.save(existingToken);
            }
        }
        
        // إنشاء token جديد
        DeviceToken deviceToken = DeviceToken.builder()
            .user(user)
            .deviceToken(request.getDeviceToken())
            .deviceType(request.getDeviceType() != null ? request.getDeviceType() : "WEB")
            .isActive(true)
            .build();
        
        DeviceToken saved = deviceTokenRepository.save(deviceToken);
        logger.info("Device token registered for user: {}", userId);
        return saved;
    }
    
    /**
     * إلغاء تسجيل device token
     */
    public void unregisterDeviceToken(Long userId, String deviceToken) {
        DeviceToken token = deviceTokenRepository
            .findByDeviceTokenAndIsActiveTrue(deviceToken)
            .orElseThrow(() -> new ResourceNotFoundException("Device token not found"));
        
        if (!token.getUser().getId().equals(userId)) {
            throw new SecurityException("You are not authorized to unregister this device token");
        }
        
        token.setIsActive(false);
        deviceTokenRepository.save(token);
        logger.info("Device token unregistered for user: {}", userId);
    }
    
    /**
     * الحصول على جميع device tokens للمستخدم
     */
    public List<DeviceToken> getUserDeviceTokens(Long userId) {
        return deviceTokenRepository.findByUserIdAndIsActiveTrue(userId);
    }
}


