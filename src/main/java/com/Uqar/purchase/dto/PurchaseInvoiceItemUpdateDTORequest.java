package com.Uqar.purchase.dto;

import com.Uqar.product.Enum.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;

@Data
@Schema(description = "Purchase Invoice Item Update Request", example = """
{
  "productId": 1,
  "receivedQty": 100,
  "bonusQty": 10,
  "invoicePrice": 5.50,
  "actualPrice": 5.00,
  "batchNo": "BATCH001",
  "expiryDate": "2025-12-31",
  "barcode": "1234567890123",
  "productType": "MASTER"
}
""")
public class PurchaseInvoiceItemUpdateDTORequest {
    
    @NotNull(message = "Product ID is required")
    @Schema(description = "Product ID", example = "1")
    private Long productId;
    
    @NotNull(message = "Product type is required")
    @Schema(description = "Product type", example = "MASTER", 
            allowableValues = {"MASTER", "PHARMACY"})
    private ProductType productType;
    
    @NotNull(message = "Received quantity is required")
    @Min(value = 1, message = "Received quantity must be at least 1")
    @Schema(description = "Quantity received", example = "100")
    private Integer receivedQty;
    
    @Min(value = 0, message = "Bonus quantity cannot be negative")
    @Schema(description = "Bonus quantity", example = "10")
    private Integer bonusQty;
    
    @NotNull(message = "Invoice price is required")
    @Min(value = 0, message = "Invoice price cannot be negative")
    @Schema(description = "Price from invoice", example = "5.50")
    private Double invoicePrice;
    
    @NotNull(message = "Actual price is required")
    @Min(value = 0, message = "Actual price cannot be negative")
    @Schema(description = "Actual price paid", example = "5.00")
    private Double actualPrice;
    
    @Schema(description = "Batch number", example = "BATCH001")
    private String batchNo;
    
    @Schema(description = "Expiry date", example = "2025-12-31")
    private LocalDate expiryDate;
    
    @Schema(description = "Product barcode", example = "1234567890123")
    private String barcode;
}
