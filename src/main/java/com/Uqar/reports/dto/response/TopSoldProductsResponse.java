package com.Uqar.reports.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopSoldProductsResponse {
    
    private Long productId;
    private String productName;
    private String productCode;
    private String categoryName;
    private String manufacturerName;
    private Long totalQuantitySold;
    private BigDecimal totalRevenue;
    private Long areaId;
    private String areaName;
    private Integer rank;
    
    public TopSoldProductsResponse(Long productId, String productName, String productCode, 
                                 String categoryName, String manufacturerName, 
                                 Long totalQuantitySold, BigDecimal totalRevenue, 
                                 Long areaId, String areaName) {
        this.productId = productId;
        this.productName = productName;
        this.productCode = productCode;
        this.categoryName = categoryName;
        this.manufacturerName = manufacturerName;
        this.totalQuantitySold = totalQuantitySold;
        this.totalRevenue = totalRevenue;
        this.areaId = areaId;
        this.areaName = areaName;
    }
}
