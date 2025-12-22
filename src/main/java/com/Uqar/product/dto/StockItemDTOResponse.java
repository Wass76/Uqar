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
public class StockItemDTOResponse {
    private Long id;
    private Long productId;
    private String productName;
    private ProductType productType;
    private List<String> barcodes;
    private Integer quantity;
    private Integer bonusQty;


    private Integer total; 
    private String supplier; 
    private List<String> categories; 
    //private Integer minStockLevel; 

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    private String batchNo;
    private Double actualPurchasePrice;
    private Float sellingPrice;
 
    // New fields for currency conversion
    private String requestedCurrency;
    private Boolean pricesConverted;
    private Float sellingPriceUSD;
    private Double actualPurchasePriceUSD;
    private Double exchangeRateSYPToUSD;
    private String conversionTimestampSYPToUSD;
    private String rateSource;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateAdded;
    private Long addedBy;
    private Long purchaseInvoiceId;
    
    private Boolean isExpired;
    private Boolean isExpiringSoon;
    private Integer daysUntilExpiry;
    
    private Long pharmacyId;
    private String purchaseInvoiceNumber;
    
    private String reason; // سبب الإضافة (للتعديلات بدون فاتورة)
    private String notes; // ملاحظات إضافية
    
    private Integer numberOfPartsPerBox;
    private Integer remainingParts; 
} 