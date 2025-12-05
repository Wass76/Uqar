package com.Uqar.sale.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Refunded Item")
public class SaleRefundItemDTOResponse {
           
    @Schema(description = "Product name", example = "Paracetamol 500mg")
    private String productName;
    
    @Schema(description = "Refunded quantity", example = "2")
    private Integer quantity;
    
    @Schema(description = "Unit price", example = "25.0")
    private Float unitPrice;
    
    @Schema(description = "Subtotal", example = "50.0")
    private Float subtotal;
    
    @Schema(description = "Item refund reason", example = "Defective product")
    private String itemRefundReason;
}

