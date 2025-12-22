package com.Uqar.product.dto;


import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    
    private Integer numberOfPartsPerBox;

    private Long typeId;
    private String type;

    private Long formId;    
    private String form;

    private Long manufacturerId;
    private String manufacturer;

    private Set<Long> categoryIds;
    private Set<String> categories;


}
