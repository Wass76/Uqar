package com.Uqar.product.dto;

import java.time.LocalDate;
import java.util.List;

import com.Uqar.product.Enum.ProductType;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockProductOverallDTOResponse {
    
    // Product Identification
    private Long id;
    private Long productId;
    private String productName; // الاسم حسب اللغة المطلوبة (للتوافق مع الكود القديم)
    private String productNameAr; // الاسم العربي
    private String productNameEn; // الاسم الإنجليزي
    private ProductType productType;
    private List<String> barcodes;
    
    // Stock Summary
    private Integer totalQuantity;           
    private Integer totalBonusQuantity;     
    private Double actualPurchasePrice;     
    private Double totalValue;               
    
    // Product Information
    private List<String> categories;
    private Float sellingPrice;
    
    // Stock Status
    private Integer minStockLevel;
    private Boolean hasExpiredItems;
    private Boolean hasExpiringSoonItems;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate earliestExpiryDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate latestExpiryDate;
    
    // Additional Info
    private Integer numberOfBatches;
    private Long pharmacyId;
    
    // Currency Support Fields - Simplified for dual currency display
    private Boolean dualCurrencyDisplay;
    
    // USD Price Fields (when dual currency is requested)
    private Double actualPurchasePriceUSD;
    private Double totalValueUSD;
    private Float sellingPriceUSD;
    private Double exchangeRateSYPToUSD;
    private String conversionTimestampSYPToUSD;
    private String rateSource;
    
    /**
     * Check if dual currency display is enabled
     */
    public boolean isDualCurrencyDisplay() {
        return dualCurrencyDisplay != null && dualCurrencyDisplay;
    }
}
