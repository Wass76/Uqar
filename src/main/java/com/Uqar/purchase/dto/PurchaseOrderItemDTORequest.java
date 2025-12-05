package com.Uqar.purchase.dto;

import com.Uqar.product.Enum.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Purchase Order Item Request", example = """
{
  "productId": 1,
  "quantity": 100,
  "price": 5.50,
  "barcode": "1234567890123",
  "productType": "MASTER"
}
""")
public class PurchaseOrderItemDTORequest {
    
    @Schema(description = "Product ID", example = "1")
    private Long productId;
    
    @Schema(description = "Quantity to purchase", example = "100")
    private Integer quantity;
    
    @Schema(description = "Unit price", example = "5.50")
    private Double price;
    
    @Schema(description = "Product barcode", example = "1234567890123")
    private String barcode;
    
    @Schema(description = "Product type", example = "MASTER", 
            allowableValues = {"MASTER", "PHARMACY"})
    private ProductType productType; // 'MASTER' or 'PHARMACY'
} 