package com.Uqar.purchase.dto;

import lombok.Data;

@Data
public class PurchaseOrderItemDTOResponse {
    private Long id;
    private String productName;
    private Integer quantity;
    private Double price;
    private String barcode;
    private Long productId;
    private com.Uqar.product.Enum.ProductType productType;
    private Double refSellingPrice;
    private Integer minStockLevel;
} 