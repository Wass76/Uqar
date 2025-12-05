package com.Uqar.product.dto;

import com.Uqar.product.Enum.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockItemWithProductInfoDTOResponse {
    private Long id;
    private Long productId;
    private ProductType productType;
    private Integer quantity;
    private Double actualPurchasePrice;
    private LocalDate expiryDate;
    private LocalDate dateAdded;
    private String productName;
    private String batchNo;
    private Integer bonusQty;
    private Long addedBy;
    private Long purchaseInvoiceId;
    private Boolean requiresPrescription;
} 