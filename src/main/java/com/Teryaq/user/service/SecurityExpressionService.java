package com.Teryaq.user.service;

import com.Teryaq.user.entity.User;
import com.Teryaq.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service("authz")
public class SecurityExpressionService extends BaseSecurityService {



    public SecurityExpressionService(UserRepository userRepository) {
        super(userRepository);
    }

    /**
     * Checks if the current user is a Pharmacy Manager
     */
    public boolean isPharmacyManager() {
        User currentUser = getCurrentUser();
        return currentUser.getRole().getName().equals("PHARMACY_MANAGER");
    }

    /**
     * Checks if the current user is a Pharmacist
     */
    public boolean isPharmacist() {
        User currentUser = getCurrentUser();
        return currentUser.getRole().getName().equals("PHARMACIST");
    }

    /**
     * Checks if the current user is a Trainee
     */
    public boolean isTrainee() {
        User currentUser = getCurrentUser();
        return currentUser.getRole().getName().equals("TRAINEE");
    }

    /**
     * Checks if the current user has a specific permission
     * @param permissionName The permission to check for
     * @return true if the user has the permission
     */
    public boolean hasPermission(String permissionName) {
        User currentUser = getCurrentUser();
        // Check role permissions
        boolean hasRolePermission = currentUser.getRole().getPermissions().stream()
                .anyMatch(permission -> permission.getName().equals(permissionName));
        if (hasRolePermission) {
            return true;
        }
        // Check additional permissions
        return currentUser.getAdditionalPermissions().stream()
                .anyMatch(permission -> permission.getName().equals(permissionName));
    }
} 