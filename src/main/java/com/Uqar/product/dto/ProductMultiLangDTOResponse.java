package com.Uqar.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductMultiLangDTOResponse {
    private Long id;
    
    private String tradeNameAr;
    private String tradeNameEn;
    
    private String scientificNameAr;
    private String scientificNameEn;

    private Long pharmacyId; 
} 