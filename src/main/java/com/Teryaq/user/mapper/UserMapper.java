package com.Teryaq.user.mapper;

import com.Teryaq.user.dto.UserResponseDTO;
import com.Teryaq.user.entity.User;
import com.Teryaq.user.entity.Employee;
import com.Teryaq.user.entity.Pharmacy;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    public UserMapper(RoleMapper roleMapper, PermissionMapper permissionMapper) {
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
    }

    public UserResponseDTO toResponse(User user) {
        if (user == null) {
            return null;
        }

        UserResponseDTO response = new UserResponseDTO();
        
        // Set basic user fields
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setPosition(user.getPosition());
        
        // Set role and permissions
        if (user.getRole() != null) {
            response.setRole(roleMapper.toResponse(user.getRole()));
        }
        
        if (user.getAdditionalPermissions() != null) {
            response.setAdditionalPermissions(user.getAdditionalPermissions().stream()
                    .map(permissionMapper::toResponse)
                    .collect(Collectors.toSet()));
        }

        // Add pharmacy information if user is an employee
        if (user instanceof Employee employee) {
            Pharmacy pharmacy = employee.getPharmacy();
            if (pharmacy != null) {
                response.setPharmacyId(pharmacy.getId());
                response.setPharmacyName(pharmacy.getName());
                
                // Use pharmacy's isActive field if available, otherwise calculate it
                if (pharmacy.getIsActive() != null) {
                    response.setIsAccountActive(pharmacy.getIsActive());
                } else {
                    boolean isAccountActive = isPharmacyAccountActive(pharmacy);
                    response.setIsAccountActive(isAccountActive);
                }
            } else {
                response.setPharmacyId(null);
                response.setPharmacyName(null);
                response.setIsAccountActive(false);
            }
        } else {
            // For non-employee users (like platform admin), set default values
            response.setPharmacyId(null);
            response.setPharmacyName(null);
            response.setIsAccountActive(true); // Platform admin accounts are always active
        }

        return response;
    }
    
    /**
     * Determines if a pharmacy account is active based on completion of registration data
     * @param pharmacy The pharmacy entity
     * @return true if the pharmacy has complete registration data
     */
    private boolean isPharmacyAccountActive(Pharmacy pharmacy) {
        if (pharmacy == null) {
            return false;
        }
        
        // Check if essential pharmacy data is complete
        // A pharmacy is considered active if it has basic required information
        return pharmacy.getName() != null && !pharmacy.getName().trim().isEmpty() &&
               pharmacy.getLicenseNumber() != null && !pharmacy.getLicenseNumber().trim().isEmpty() &&
               pharmacy.getAddress() != null && !pharmacy.getAddress().trim().isEmpty() &&
               pharmacy.getPhoneNumber() != null && !pharmacy.getPhoneNumber().trim().isEmpty();
    }
} 