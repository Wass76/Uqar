package com.Uqar.user.dto;

import com.Uqar.utils.annotation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PharmacyCreateRequestDTO {
    @NotBlank(message = "Pharmacy name couldn't be blank")
    private String pharmacyName;
    @NotBlank(message = "License number couldn't be blank")
    private String licenseNumber;
    @NotBlank(message = "Phone number couldn't be blank")
    private String phoneNumber;
    @NotBlank(message = "Manager password couldn't be blank")
    @ValidPassword
    private String managerPassword;
}