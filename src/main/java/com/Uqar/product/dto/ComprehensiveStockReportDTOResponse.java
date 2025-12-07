package com.Uqar.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprehensiveStockReportDTOResponse {
    private Map<String, Object> pharmacyProducts;
    private Map<String, Object> masterProducts;
    private List<StockItemDTOResponse> expiredItems;
    private List<StockItemDTOResponse> expiringSoonItems;
} 