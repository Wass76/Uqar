package com.Uqar.product.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PharmacyProductListDTO {

    private Long id;
    private String tradeName;
    private String scientificName;
    private String concentration;
    private String size;
    private Integer minStockLevel;
    private Boolean requiresPrescription;
    private Set<String> barcodes;
    private String productTypeName;
        
    private Long pharmacyId;
    private String pharmacyName;
} 