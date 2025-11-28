package com.Teryaq.notification.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Teryaq.notification.entity.Notification;
import com.Teryaq.notification.enums.NotificationType;
import com.Teryaq.user.entity.User;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    List<Notification> findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(Long userId);
    
    Long countByUserIdAndReadAtIsNull(Long userId);
    
    List<Notification> findByNotificationTypeAndStatus(NotificationType type, String status);
    
    List<Notification> findByStatusAndSentAtBefore(String status, LocalDateTime before);
    
    // البحث عن الإشعارات الفاشلة لإعادة المحاولة
    List<Notification> findByStatusAndCreatedAtBefore(String status, LocalDateTime before);
    
    // البحث عن الإشعارات المعلقة (PENDING) القديمة
    List<Notification> findByStatusAndCreatedAtBeforeOrderByCreatedAtAsc(String status, LocalDateTime before);
    
    // البحث عن الإشعارات المعلقة للمعالجة (Database Queue)
    Page<Notification> findByStatusOrderByCreatedAtAsc(String status, Pageable pageable);
    
    // البحث عن الإشعارات الجاهزة للمحاولة (next_retry_at <= now() أو null)
    @Query("SELECT n FROM Notification n WHERE n.status = :status " +
           "AND (n.nextRetryAt IS NULL OR n.nextRetryAt <= :now) " +
           "ORDER BY n.createdAt ASC")
    Page<Notification> findReadyForRetry(@Param("status") String status, 
                                         @Param("now") LocalDateTime now, 
                                         Pageable pageable);
}


