package com.Uqar.purchase.dto;

import com.Uqar.product.Enum.OrderStatus;
import com.Uqar.user.Enum.Currency;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseOrderDTOResponse {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private Currency currency;
    private Double total;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private Long createdBy;
    private List<PurchaseOrderItemDTOResponse> items;
} 