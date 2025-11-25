package com.Teryaq.notification.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Teryaq.notification.entity.DeviceToken;
import com.Teryaq.user.entity.User;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    
    List<DeviceToken> findByUserAndIsActiveTrue(User user);
    
    List<DeviceToken> findByUserIdAndIsActiveTrue(Long userId);
    
    Optional<DeviceToken> findByDeviceTokenAndIsActiveTrue(String deviceToken);
    
    List<DeviceToken> findByIsActiveTrue();
}

