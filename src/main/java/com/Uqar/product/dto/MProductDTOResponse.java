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
public class MProductDTOResponse {

    private Long id;
    private String tradeName;
    private String scientificName;
    private String concentration;
    private String size;
    private float refPurchasePrice;
    private float refSellingPrice;
    private Integer minStockLevel;
    private String notes;
    private float tax;
    private String barcode;

   
    private String productTypeName;

    @Builder.Default
    private Boolean requiresPrescription = false;

    private Long typeId;
    private String type;

    private Long formId;    
    private String form;

    private Long manufacturerId;
    private String manufacturer;

    private Set<Long> categoryIds;
    private Set<String> categories;


}
