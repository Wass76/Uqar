package com.Uqar.product.dto;

import java.time.LocalDate;

import com.Uqar.product.Enum.ProductType;
import com.Uqar.user.Enum.Currency;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Partial Inventory Adjustment Request", example = """
{
    "productId": 1,
    "productType": "PHARMACY",
    "newQuantity": 150,
    "newExpiryDate": "2025-12-31",
    "actualPurchasePrice": 100.0,
    "currency": "SYP"
}
""")
public class PartialInventoryAdjustmentRequest {
    
    @NotNull(message = "Product ID is required")
    @Schema(description = "Product ID to adjust", example = "1")
    private Long productId;
    
    @NotNull(message = "Product type is required")
    @Schema(description = "Product type (MASTER or PHARMACY)", example = "PHARMACY")
    private ProductType productType;
    
    @NotNull(message = "New quantity is required")
    @Min(value = 1, message = "New quantity must be greater than 0")
    @Schema(description = "New quantity (must be greater than 0)", example = "150")
    private Integer newQuantity;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "New expiry date (optional - if not provided, will keep existing or set to null)", example = "2025-12-31")
    private LocalDate newExpiryDate;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Purchase price must be greater than 0")
    @Schema(description = "Purchase price (optional - if not provided, will use refPurchasePrice from product). If provided, currency conversion will be applied.", example = "100.0")
    private Double actualPurchasePrice;
    
    @Schema(description = "Currency for actualPurchasePrice (optional - default: SYP). Ignored if actualPurchasePrice is not provided.", example = "SYP")
    private Currency currency;
    
    @Schema(description = "Minimum stock level (optional)", example = "10")
    private Integer minStockLevel;
}

