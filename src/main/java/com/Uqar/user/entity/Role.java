package com.Uqar.user.entity;

import com.Uqar.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Entity
@Table(name = "roles")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Role extends AuditedEntity {
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column
    private String description;
    
    @Column(nullable = false)
    private boolean isActive;
    
    @Column(nullable = false)
    private boolean isSystem;
    
    @Column(nullable = false)
    private boolean isSystemGenerated;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;

    @Override
    protected String getSequenceName() {
        return "roles_id_seq";
    }
} 