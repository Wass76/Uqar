package com.Uqar.complaint.entity;

import com.Uqar.complaint.enums.ComplaintStatus;
import com.Uqar.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Complaint extends AuditedEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "pharmacy_id", nullable = false)
    private Long pharmacyId;

    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ComplaintStatus status = ComplaintStatus.PENDING;
    
    @Column(name = "response", columnDefinition = "TEXT")
    private String response;
    
    @Column(name = "responded_by")
    private Long respondedBy;
    
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
    
    // Additional audit fields for comprehensive tracking
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "session_id", length = 100)
    private String sessionId;
    
    @Column(name = "user_type", length = 50)
    private String userType;
    
    @Column(name = "additional_data", columnDefinition = "TEXT")
    private String additionalData;
}
