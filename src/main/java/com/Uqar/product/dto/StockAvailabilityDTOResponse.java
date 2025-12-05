package com.Uqar.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAvailabilityDTOResponse {
    private Long productId;
    private Integer requiredQuantity;
    private Boolean isAvailable;
    private Integer availableQuantity;
} 