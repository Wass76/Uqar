package com.Uqar.notification.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Uqar.notification.dto.NotificationRequest;
import com.Uqar.notification.dto.NotificationResponse;
import com.Uqar.notification.entity.Notification;
import com.Uqar.notification.mapper.NotificationMapper;
import com.Uqar.notification.repository.NotificationRepository;
import com.Uqar.product.dto.PaginationDTO;
import com.Uqar.user.entity.User;
import com.Uqar.user.repository.UserRepository;
import com.Uqar.user.service.BaseSecurityService;
import com.Uqar.utils.exception.ResourceNotFoundException;

@Service
@Transactional
public class NotificationService extends BaseSecurityService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationProducer notificationProducer;
    private final UserRepository userRepository;
    
    public NotificationService(UserRepository userRepository,
                               NotificationRepository notificationRepository,
                               NotificationMapper notificationMapper,
                               NotificationProducer notificationProducer) {
        super(userRepository);
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.notificationProducer = notificationProducer;
        this.userRepository = userRepository;
    }
    
    /**
     * إرسال إشعار لمستخدم محدد
     */
    public NotificationResponse sendNotification(NotificationRequest request) {
        Notification notification = notificationProducer.enqueue(request);
        logger.info("Notification {} enqueued for user {}", notification.getId(), request.getUserId());
        return notificationMapper.toResponse(notification);
    }
    
    /**
     * الحصول على إشعارات المستخدم (مع pagination محسّن)
     */
    public PaginationDTO<NotificationResponse> getUserNotificationsPaginated(int page, int size) {
        Long currentUserId = getCurrentUser().getId();
        User user = userRepository.findById(currentUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        
        List<NotificationResponse> responses = notifications.getContent().stream()
            .map(notificationMapper::toResponse)
            .collect(Collectors.toList());
        
        return new PaginationDTO<>(responses, page, size, notifications.getTotalElements());
    }
    
    /**
     * الحصول على إشعارات المستخدم (للتوافق مع الكود القديم)
     */
    public Page<NotificationResponse> getUserNotifications(Pageable pageable) {
        Long currentUserId = getCurrentUser().getId();
        User user = userRepository.findById(currentUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Page<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return notifications.map(notificationMapper::toResponse);
    }
    
    /**
     * الحصول على الإشعارات غير المقروءة (مع pagination محسّن)
     */
    public PaginationDTO<NotificationResponse> getUnreadNotificationsPaginated(int page, int size) {
        Long currentUserId = getCurrentUser().getId();
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository
            .findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(currentUserId, pageable);
        
        List<NotificationResponse> responses = notifications.getContent().stream()
            .map(notificationMapper::toResponse)
            .collect(Collectors.toList());
        
        return new PaginationDTO<>(responses, page, size, notifications.getTotalElements());
    }
    
    /**
     * الحصول على الإشعارات غير المقروءة (للتوافق مع الكود القديم)
     */
    public List<NotificationResponse> getUnreadNotifications() {
        Long currentUserId = getCurrentUser().getId();
        List<Notification> notifications = notificationRepository
            .findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(currentUserId);
        return notifications.stream()
            .map(notificationMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * تحديد إشعار كمقروء
     */
    public NotificationResponse markAsRead(Long notificationId) {
        Long currentUserId = getCurrentUser().getId();
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        
        if (!notification.getUser().getId().equals(currentUserId)) {
            throw new SecurityException("You are not authorized to access this notification");
        }
        
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
        
        return notificationMapper.toResponse(notification);
    }
    
    /**
     * الحصول على عدد الإشعارات غير المقروءة
     */
    public Long getUnreadCount() {
        Long currentUserId = getCurrentUser().getId();
        return notificationRepository.countByUserIdAndReadAtIsNull(currentUserId);
    }
}

