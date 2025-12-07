package com.Uqar.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String position;
    private RoleResponseDTO role;
    private Set<PermissionResponseDTO> additionalPermissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    
    // Pharmacy information for employees
    private Long pharmacyId;
    private String pharmacyName;
    
    // Account activation status
    private Boolean isAccountActive;
} 