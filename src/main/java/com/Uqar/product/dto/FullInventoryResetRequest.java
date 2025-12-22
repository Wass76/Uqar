package com.Uqar.product.dto;

import java.time.LocalDate;
import java.util.List;

import com.Uqar.product.Enum.ProductType;
import com.Uqar.user.Enum.Currency;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO للجرد الكامل
 * Full Inventory Reset Request DTO
 * 
 * يستخدم لحذف جميع سجلات المخزون وإعادة إدخالها من الصفر
 * Used to delete all stock records and re-enter them from scratch
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Full Inventory Reset Request", example = """
{
    "items": [
        {
            "productId": 1,
            "productType": "PHARMACY",
            "quantity": 100,
            "expiryDate": "2025-12-31",
            "actualPurchasePrice": 100.0,
            "currency": "SYP"
        },
        {
            "productId": 2,
            "productType": "MASTER",
            "quantity": 50,
            "expiryDate": "2025-06-30"
        }
    ]
}
""")
public class FullInventoryResetRequest {
    
    @NotEmpty(message = "Items list cannot be empty. At least one item must be provided.")
    @Valid
    @Schema(description = "List of inventory items to add after reset")
    private List<InventoryItemDTO> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Single inventory item for full inventory reset")
    public static class InventoryItemDTO {
        
        @NotNull(message = "Product ID is required")
        @Schema(description = "Product ID", example = "1")
        private Long productId;
        
        @NotNull(message = "Product type is required")
        @Schema(description = "Product type (MASTER or PHARMACY)", example = "PHARMACY")
        private ProductType productType;
        
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be greater than 0")
        @Schema(description = "Quantity (must be greater than 0)", example = "100")
        private Integer quantity;
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "Expiry date (optional but recommended)", example = "2025-12-31")
        private LocalDate expiryDate;
        
        @DecimalMin(value = "0.0", inclusive = false, message = "Purchase price must be greater than 0")
        @Schema(description = "Purchase price (optional - if not provided, will use refPurchasePrice from product). If provided, currency conversion will be applied.", example = "100.0")
        private Double actualPurchasePrice;
        
        @Schema(description = "Currency for actualPurchasePrice (optional - default: SYP). Ignored if actualPurchasePrice is not provided.", example = "SYP")
        private Currency currency;
        
        @Schema(description = "Minimum stock level (optional)", example = "10")
        private Integer minStockLevel;
    }
}

