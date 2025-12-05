package com.Uqar.user.entity;

import com.Uqar.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Permission extends AuditedEntity {
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column
    private String description;
    
    @Column(nullable = false)
    private String resource; // e.g., "LEAD", "CONTACT", "DEALER"
    
    @Column(nullable = false)
    private String action; // e.g., "CREATE", "READ", "UPDATE", "DELETE"
    
    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = false)
    private boolean isSystemGenerated;

    @Override
    protected String getSequenceName() {
        return "permissions_id_seq";
    }
} 