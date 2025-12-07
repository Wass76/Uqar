package com.Uqar.purchase.dto;

import com.Uqar.user.Enum.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@Schema(description = "Purchase Order Update Request", example = """
{
  "supplierId": 1,
  "currency": "USD",
  "items": [
    {
      "productId": 1,
      "quantity": 100,
      "price": 5.50,
      "barcode": "1234567890123",
      "productType": "MASTER"
    }
  ]
}
""")
public class PurchaseOrderUpdateDTORequest {
    
    @NotNull(message = "Supplier ID is required")
    @Schema(description = "Supplier ID", example = "1")
    private Long supplierId;
    
    @NotNull(message = "Currency is required")
    @Schema(description = "Currency for the purchase order", example = "USD", 
            allowableValues = {"USD", "EUR", "GBP", "SAR", "AED"})
    private Currency currency;
    
    @NotNull(message = "Items list is required")
    @Size(min = 1, message = "At least one item is required")
    @Valid
    @Schema(description = "List of items to purchase")
    private List<PurchaseOrderItemDTORequest> items;
}
