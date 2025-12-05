package com.Uqar.user.dto;

import com.Uqar.user.Enum.PharmacyType;
import lombok.Data;

@Data
public class PharmacyResponseDTO {
    private Long id;
    private String pharmacyName;
    private String licenseNumber;
    private String address;
    private String email;
    private PharmacyType type;
    private String openingHours;
    private String phoneNumber;
    private String managerEmail;
    private String managerFirstName;
    private String managerLastName;
    private String areaName;
    private Long areaId;
    private String areaArabicName;
    
    // Account activation status
    private Boolean isActive;
} 