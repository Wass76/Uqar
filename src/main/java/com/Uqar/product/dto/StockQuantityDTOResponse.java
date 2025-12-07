package com.Uqar.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockQuantityDTOResponse {
    private Long productId;
    private Integer totalQuantity;
} 