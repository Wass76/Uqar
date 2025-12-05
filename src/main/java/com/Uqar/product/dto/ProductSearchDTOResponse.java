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
public class ProductSearchDTOResponse {
    private Long id;
    private String tradeName; // الاسم التجاري حسب اللغة المطلوبة (للتوافق مع الكود القديم)
    private String tradeNameAr; // الاسم التجاري العربي
    private String tradeNameEn; // الاسم التجاري الإنجليزي
    private String scientificName; // الاسم العلمي حسب اللغة المطلوبة (للتوافق مع الكود القديم)
    private String scientificNameAr; // الاسم العلمي العربي
    private String scientificNameEn; // الاسم العلمي الإنجليزي
    private Set<String> barcodes;
   
    private String productTypeName;
    private Boolean requiresPrescription;
    private String concentration;
    private String size;
    private float refPurchasePrice;
    private float refSellingPrice;
    private Integer minStockLevel;
    
    private Long pharmacyId;
    private String pharmacyName;
    
    private Long typeId;
    private String type;

    private Long formId;
    private String form;   

    private Long manufacturerId;
    private String manufacturer;

    private Set<Long> categoryIds;
    private Set<String> categories;

    private String notes;
    private float tax;
    private Integer quantity;
    
    // Currency Support Fields - Simplified for dual currency display
    private Boolean dualCurrencyDisplay;
    
    // USD Price Fields (when dual currency is requested)
    private Double refPurchasePriceUSD;
    private Double refSellingPriceUSD;
    private Double exchangeRateSYPToUSD;
    
    /**
     * Check if dual currency display is enabled
     */
    public boolean isDualCurrencyDisplay() {
        return dualCurrencyDisplay != null && dualCurrencyDisplay;
    }
} 