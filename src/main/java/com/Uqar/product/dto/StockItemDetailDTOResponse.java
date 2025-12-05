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
public class StockItemDetailDTOResponse {
    
    // Product Identification
    private Long id;
    private String productName;
    private String batchNumber;
    private ProductType productType;
    private List<String> barcodes;
    
    // Stock Information
    private Integer currentStock;
   // private Integer minStockLevel; 
    private Double actualPurchasePrice; 
    private Double totalValue; 
    
    // Product Information
    private List<String> categories;
    private Boolean requiresPrescription;
    private String supplier;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
}
