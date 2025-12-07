package com.Uqar.sale.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SaleInvoiceItemDTOResponse {
    private Long id;
    private Long stockItemId;
    private String productName;
    private Integer quantity;
    private Integer refundedQuantity;
    private Integer availableForRefund;
    private Float unitPrice;
    private Float subTotal;
} 