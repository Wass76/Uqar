package com.Uqar.product.dto;

import com.Uqar.product.Enum.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockReportDTOResponse {
    private ProductType productType;
    private Integer totalItems;
    private Integer totalQuantity;
    private Double totalValue;
    private Long expiredItems;
    private Long expiringSoonItems;
    private List<StockItemDTOResponse> stockItems;
} 