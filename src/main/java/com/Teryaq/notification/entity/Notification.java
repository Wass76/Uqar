package com.Teryaq.notification.entity;

import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.Teryaq.notification.enums.NotificationType;
import com.Teryaq.user.entity.User;
import com.Teryaq.utils.entity.AuditedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "notification")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends AuditedEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    
    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", length = 50)
    private NotificationType notificationType;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "jsonb")
    private Map<String, Object> data; // معلومات إضافية
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, SENT, FAILED
    
    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0; // عدد محاولات إعادة الإرسال
    
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt; // وقت المحاولة القادمة (exponential backoff)
    
    @Override
    protected String getSequenceName() {
        return "notification_id_seq";
    }
}

