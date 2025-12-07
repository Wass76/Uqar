package com.Uqar.user.service;

import com.Uqar.user.entity.Employee;
import com.Uqar.user.entity.Pharmacy;
import com.Uqar.user.entity.User;
import com.Uqar.user.repository.UserRepository;
import com.Uqar.utils.exception.ResourceNotFoundException;
import com.Uqar.utils.exception.UnAuthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public abstract class BaseSecurityService {

    protected final UserRepository userRepository;

    protected BaseSecurityService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Gets the currently authenticated user
     * @return The current user
     * @throws ResourceNotFoundException if the user is not found
     */
    protected User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Checks if the current user is a platform admin
     * @return true if the user is a platform admin
     */
    protected boolean isAdmin() {
        User currentUser = getCurrentUser();
        return currentUser.getRole().getName().equals("PLATFORM_ADMIN");
    }

    /**
     * Checks if the current user has a specific role
     * @param roleName The name of the role to check
     * @return true if the user has the specified role
     */
    protected boolean hasRole(String roleName) {
        User currentUser = getCurrentUser();
        return currentUser.getRole().getName().equals(roleName);
    }

    /**
     * Gets the pharmacy associated with the currently authenticated user
     * @return Pharmacy object
     * @throws UnAuthorizedException if user is not an employee or has no pharmacy
     */
    protected Pharmacy getCurrentUserPharmacy() {
        User currentUser = getCurrentUser();
        if (currentUser instanceof Employee employee) {
            if (employee.getPharmacy() == null) {
                throw new UnAuthorizedException("User is not associated with any pharmacy");
            }
            return employee.getPharmacy();
        }
        throw new UnAuthorizedException("User is not an employee");
    }

    /**
     * Gets the pharmacy ID of the currently authenticated user
     * @return Pharmacy ID
     * @throws UnAuthorizedException if user is not an employee or has no pharmacy
     */
    protected Long getCurrentUserPharmacyId() {
        return getCurrentUserPharmacy().getId();
    }

    /**
     * Validates that the current user has access to the specified pharmacy
     * @param pharmacyId The pharmacy ID to validate access for
     * @throws UnAuthorizedException if user doesn't have access
     */
    protected void validatePharmacyAccess(Long pharmacyId) {
        Long currentUserPharmacyId = getCurrentUserPharmacyId();
        if (!currentUserPharmacyId.equals(pharmacyId)) {
            throw new UnAuthorizedException("User does not have access to pharmacy with ID: " + pharmacyId);
        }
    }

    /**
     * Validates that the current user has access to the specified pharmacy
     * @param pharmacy The pharmacy object to validate access for
     * @throws UnAuthorizedException if user doesn't have access
     */
    protected void validatePharmacyAccess(Pharmacy pharmacy) {
        if (pharmacy == null) {
            throw new UnAuthorizedException("Pharmacy is null");
        }
        validatePharmacyAccess(pharmacy.getId());
    }

    /**
     * Checks if the current user is an employee
     * @return true if user is an employee
     */
    protected boolean isCurrentUserEmployee() {
        try {
            User currentUser = getCurrentUser();
            return currentUser instanceof Employee;
        } catch (Exception e) {
            return false;
        }
    }
} 