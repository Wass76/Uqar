package com.Uqar.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO لإحصائية الجرد
 * Inventory Count Summary Response DTO
 * 
 * يحتوي على إحصائية عدد الأدوية الكلي في الصيدلية
 * Contains statistics for total number of medicines in the pharmacy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Inventory Count Summary Response")
public class InventoryCountSummaryResponse {
    
    @Schema(description = "Total number of unique products in pharmacy", example = "250")
    private Long totalProducts;
    
    @Schema(description = "Total quantity of all stock items", example = "5000")
    private Integer totalQuantity;
    
    @Schema(description = "Total number of stock items (batches)", example = "350")
    private Long totalStockItems;
}

