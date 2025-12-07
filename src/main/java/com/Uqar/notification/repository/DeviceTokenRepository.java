package com.Uqar.notification.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Uqar.notification.entity.DeviceToken;
import com.Uqar.user.entity.User;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    
    List<DeviceToken> findByUserAndIsActiveTrue(User user);
    
    @Query("SELECT dt FROM DeviceToken dt WHERE dt.user.id = :userId AND dt.isActive = true")
    List<DeviceToken> findByUserIdAndIsActiveTrue(@Param("userId") Long userId);
    
    Optional<DeviceToken> findByDeviceTokenAndIsActiveTrue(String deviceToken);
    
    List<DeviceToken> findByIsActiveTrue();
}

