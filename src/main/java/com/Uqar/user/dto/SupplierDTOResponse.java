package com.Uqar.user.dto;

import com.Uqar.user.Enum.Currency;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class SupplierDTOResponse {
    private Long id;
    private String name;
    private String phone;
    private String address;
    @Enumerated(EnumType.STRING)
    private Currency preferredCurrency;
} 