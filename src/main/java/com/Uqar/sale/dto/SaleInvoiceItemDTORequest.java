package com.Uqar.sale.dto;

//import com.Uqar.product.Enum.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Sale Invoice Item Request", example = """
{
  "stockItemId": 1,
  "quantity": 2,
  "unitPrice": 800.0
}
""")
public class SaleInvoiceItemDTORequest {
    @NotNull(message = "Stock item ID is required")
    @Min(value = 1, message = "Stock item ID must be positive")
    @Schema(description = "Stock Item ID", example = "1")
    private Long stockItemId;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10000, message = "Quantity cannot exceed 10000")
    @Schema(description = "Quantity", example = "2")
    private Integer quantity;
    
    @Min(value = 0, message = "Unit price must be non-negative")
    @Schema(description = "Unit Price (optional - will use stock price if not provided)", example = "800.0")
    private Float unitPrice; 
    
    // @Schema(description = "Discount Type", example = "FIXED_AMOUNT", allowableValues = {"PERCENTAGE", "FIXED_AMOUNT"})
    // private DiscountType discountType;
    
    // @Min(value = 0, message = "Discount value must be non-negative")
    // @Max(value = 100, message = "Discount percentage cannot exceed 100%")
    // @Schema(description = "Discount Value", example = "100.0")
    // private Float discountValue;
    
    // @Min(value = 0, message = "Discount value must be non-negative")
    // @Max(value = 100, message = "Discount percentage cannot exceed 100%")
    // @Schema(description = "Discount (Legacy field)", example = "100.0")
    // private Float discount;
    
    // // للتوافق مع الكود القديم
    // public Float getDiscountValue() {
    //     if (discount != null) {
    //         return discount;
    //     }
    //     return discountValue != null ? discountValue : 0f;
    // }
    
    // public DiscountType getDiscountType() {
    //     return discountType != null ? discountType : DiscountType.FIXED_AMOUNT;
    // }
} 