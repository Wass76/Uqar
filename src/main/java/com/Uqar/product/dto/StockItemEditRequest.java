package com.Uqar.product.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "edit stock quantity request", example= """
{
    "quantity": 10,
    "minStockLevel": 5,
    "reasonCode": "Received Shipment",
    "additionalNotes": "تم استلام شحنة جديدة"
}
        """)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockItemEditRequest {

    @Schema(description = "quantity", example = "10")
    @Min(value = 1, message = "quantity must be greater than 0")
    private Integer quantity;

    @Schema(description = "expiry date", example = "2025-01-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    
    @Schema(description = "minimum stock level", example = "10")
    @Min(value = 0, message = "minimum stock level must be greater than or equal to 0")
    private Integer minStockLevel;
    
    @Schema(description = "reason code", example = "Received Shipment")
    @NotBlank(message = "reason code is required")
    private String reasonCode;
    
    @Schema(description = "additional notes", example = "تم استلام شحنة جديدة")
    private String additionalNotes;
} 