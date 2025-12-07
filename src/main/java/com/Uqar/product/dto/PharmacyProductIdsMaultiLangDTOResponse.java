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
public class PharmacyProductIdsMaultiLangDTOResponse {

    private Long id;
    private String tradeNameAr;
    private String tradeNameEn;
    private String scientificNameAr;
    private String scientificNameEn;
    private String concentration;
    private String size;
    private float refPurchasePrice;
    private float refSellingPrice;
    private Integer minStockLevel;
    private String notes;
    private float tax;
    private Set<String> barcodes;

  
    
    private String productTypeNameAr;
    private String productTypeNameEn;

    @Builder.Default
    private Boolean requiresPrescription = false;

    private Long pharmacyId;
    private String pharmacyName;

    private Long typeId;
    private String typeAr;
    private String typeEn;

    private Long formId;
    private String formAr;
    private String formEn;

    private Long manufacturerId;
    private String manufacturerAr;
    private String manufacturerEn;

    private Set<Long> categoryIds;
    private Set<String> categoriesAr;
    private Set<String> categoriesEn;


}
