package com.Uqar.product.dto;

import java.time.LocalDate;

import com.Uqar.product.Enum.InventoryAdjustmentReason;
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
@Schema(description = "Inventory Adjustment Request", example = """
{
    "productId": 1,
    "productType": "PHARMACY",
    "quantity": 10,
    "bonusQty": 1,
    "actualPurchasePrice": 100.0,
    "sellingPrice": 300.0,
    "currency": "SYP",
    "expiryDate": "2025-12-31",
    "batchNo": "BATCH001",
    "invoiceNumber": "INV001",
    "minStockLevel": 10,
    "reason": "INVENTORY_COUNT",
    "notes": "Adjustment notes"
}
""")

public class InventoryAdjustmentRequest {
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Product type is required")
    private ProductType productType; // MASTER or PHARMACY
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;
    
    @Min(value = 0, message = "Bonus quantity cannot be negative")
    private Integer bonusQty; // Optional, defaults to 0
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Purchase price must be greater than 0")
    private Double actualPurchasePrice; // Optional for MASTER products (will use refPurchasePrice), required for PHARMACY
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Selling price must be greater than 0")
    private Double sellingPrice; // Optional - لتحديث refSellingPrice للمنتجات PHARMACY
    
    private Currency currency; // Optional - عملة الأسعار (actualPurchasePrice و sellingPrice) - افتراضي: SYP
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate; // Optional but recommended
    
    private String batchNo; // Optional
    
    private String invoiceNumber; // Optional - يمكن أن يكون رقم فاتورة غير موجودة في النظام
    
    @Min(value = 0, message = "Minimum stock level cannot be negative")
    private Integer minStockLevel; // Optional
    
    @NotNull(message = "Reason is required for audit purposes")
    private InventoryAdjustmentReason reason; // سبب الإضافة (يجب أن يكون أحد الأسباب الثابتة)
    
    private String notes; // ملاحظات إضافية
}

