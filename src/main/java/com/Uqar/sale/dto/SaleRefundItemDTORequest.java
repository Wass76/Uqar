package com.Uqar.sale.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Refund Item")
public class SaleRefundItemDTORequest {
    
    @Schema(description = "Sale invoice item ID", example = "1")
    @NotNull(message = "Item ID is required")
    private Long itemId;
    
    @Schema(description = "Quantity to refund", example = "2")
    @NotNull(message = "Refund quantity is required")
    @Min(value = 1, message = "Refund quantity must be at least 1")
    private Integer quantity;
    
    @Schema(description = "Reason for refunding this item", example = "Defective product")
    private String itemRefundReason;
}
