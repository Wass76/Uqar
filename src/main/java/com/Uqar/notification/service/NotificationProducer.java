package com.Uqar.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Uqar.notification.dto.NotificationRequest;
import com.Uqar.notification.entity.Notification;
import com.Uqar.notification.repository.NotificationRepository;
import com.Uqar.user.entity.User;
import com.Uqar.user.repository.UserRepository;
import com.Uqar.utils.exception.ResourceNotFoundException;

/**
 * Responsible for persisting notifications to the database queue.
 * Notifications with status PENDING will be processed by NotificationQueueProcessor.
 */
@Service
@Transactional
public class NotificationProducer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationProducer.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationProducer(NotificationRepository notificationRepository,
                                UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Persists the notification to the database with status PENDING.
     * The NotificationQueueProcessor will process it asynchronously.
     */
    public Notification enqueue(NotificationRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        Notification notification = Notification.builder()
            .user(user)
            .title(request.getTitle())
            .body(request.getBody())
            .notificationType(request.getNotificationType())
            .data(request.getData())
            .status("PENDING")
            .retryCount(0)
            .build();

        notification = notificationRepository.save(notification);

        logger.info("Notification {} enqueued to database queue for user {}", notification.getId(), request.getUserId());
        return notification;
    }
}

