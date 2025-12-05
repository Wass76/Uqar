package com.Uqar.user.mapper;

import com.Uqar.user.dto.PharmacyCreateRequestDTO;
import com.Uqar.user.dto.PharmacyResponseDTO;
import com.Uqar.user.entity.Employee;
import com.Uqar.user.entity.Pharmacy;

public class PharmacyMapper {
    public static Pharmacy toEntity(PharmacyCreateRequestDTO dto) {
        if (dto == null) return null;
        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setName(dto.getPharmacyName());
        pharmacy.setLicenseNumber(dto.getLicenseNumber());
        pharmacy.setPhoneNumber(dto.getPhoneNumber());
        return pharmacy;
    }

    public static PharmacyCreateRequestDTO toDto(Pharmacy entity) {
        if (entity == null) return null;
        PharmacyCreateRequestDTO dto = new PharmacyCreateRequestDTO();
        dto.setPharmacyName(entity.getName());
        dto.setLicenseNumber(entity.getLicenseNumber());
        dto.setPhoneNumber(entity.getPhoneNumber());
        // managerPassword not set from entity
        return dto;
    }

    public static PharmacyResponseDTO toResponseDTO(Pharmacy pharmacy, Employee manager) {
        if (pharmacy == null) return null;
        PharmacyResponseDTO dto = new PharmacyResponseDTO();
        dto.setId(pharmacy.getId());
        dto.setPharmacyName(pharmacy.getName());
        dto.setLicenseNumber(pharmacy.getLicenseNumber());
        dto.setAddress(pharmacy.getAddress());
        dto.setEmail(pharmacy.getEmail());
        dto.setType(pharmacy.getType());
        dto.setOpeningHours(pharmacy.getOpeningHours());
        dto.setPhoneNumber(pharmacy.getPhoneNumber());
        if (manager != null) {
            dto.setManagerEmail(manager.getEmail());
            dto.setManagerFirstName(manager.getFirstName());
            dto.setManagerLastName(manager.getLastName());
        }
        if (pharmacy.getArea() != null){
            dto.setAreaId(pharmacy.getArea().getId());
            dto.setAreaName(pharmacy.getArea().getName());
            dto.setAreaArabicName(pharmacy.getArea().getArabicName());
        }
        
        // Set isActive from entity field, fallback to calculated value if null
        if (pharmacy.getIsActive() != null) {
            dto.setIsActive(pharmacy.getIsActive());
        } else {
            dto.setIsActive(isPharmacyAccountActive(pharmacy));
        }
        
        return dto;
    }
    
    /**
     * Determines if a pharmacy account is active based on completion of registration data
     * @param pharmacy The pharmacy entity
     * @return true if the pharmacy has complete registration data
     */
    public static boolean isPharmacyAccountActive(Pharmacy pharmacy) {
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
    
    /**
     * Updates the isActive field of a pharmacy based on registration completion
     * @param pharmacy The pharmacy entity to update
     */
    public static void updatePharmacyActiveStatus(Pharmacy pharmacy) {
        if (pharmacy != null) {
            boolean isActive = isPharmacyAccountActive(pharmacy);
            pharmacy.setIsActive(isActive);
        }
    }

    public static void updatePharmacyFromRequest(Pharmacy pharmacy, String address, String email, String openingHours) {
        if (address != null && !address.isEmpty()) {
            pharmacy.setAddress(address);
        }
        if (email != null && !email.isEmpty()) {
            pharmacy.setEmail(email);
        }
        if (openingHours != null && !openingHours.isEmpty()) {
            pharmacy.setOpeningHours(openingHours);
        }
    }
} 