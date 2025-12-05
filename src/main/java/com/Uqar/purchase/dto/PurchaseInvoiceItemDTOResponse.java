package com.Uqar.purchase.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PurchaseInvoiceItemDTOResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productType;
    private Integer receivedQty;
    private Integer bonusQty;
    private Double invoicePrice;
    private Double actualPrice;
    private String batchNo;
    private LocalDate expiryDate;
    private Double refSellingPrice;
    private Integer minStockLevel;
} 