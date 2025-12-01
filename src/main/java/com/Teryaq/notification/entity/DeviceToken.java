package com.Teryaq.notification.entity;

import com.Teryaq.user.entity.User;
import com.Teryaq.utils.entity.AuditedEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "device_token")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"createdBy", "lastModifiedBy", "createdByUserType", "lastModifiedByUserType"})  // ✅ إخفاء حقول Auditing من الـ response
public class DeviceToken extends AuditedEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore  // ✅ منع serialize الـ User proxy
    private User user;
    
    // Getter للـ userId فقط (بدون serialize الـ User كامل)
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
    
    @Column(name = "device_token", nullable = false, columnDefinition = "TEXT")
    private String deviceToken;
    
    @Column(name = "device_type", length = 20)
    private String deviceType; // ANDROID, IOS, WEB
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Override
    protected String getSequenceName() {
        return "device_token_id_seq";
    }
}

