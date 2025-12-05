package com.Uqar.purchase.dto;

import com.Uqar.user.Enum.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@Schema(description = "Purchase Invoice Update Request", example = """
{
  "supplierId": 1,
  "purchaseOrderId": 1,
  "currency": "USD",
  "items": [
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
  ]
}
""")
public class PurchaseInvoiceUpdateDTORequest {
    
    @NotNull(message = "Supplier ID is required")
    @Schema(description = "Supplier ID", example = "1")
    private Long supplierId;
    
    @NotNull(message = "Purchase order ID is required")
    @Schema(description = "Purchase order ID", example = "1")
    private Long purchaseOrderId;
    
    @NotNull(message = "Currency is required")
    @Schema(description = "Currency for the purchase invoice", example = "USD", 
            allowableValues = {"USD", "EUR", "GBP", "SAR", "AED"})
    private Currency currency;
    
    @NotNull(message = "Items list is required")
    @Size(min = 1, message = "At least one item is required")
    @Valid
    @Schema(description = "List of items in the invoice")
    private List<PurchaseInvoiceItemUpdateDTORequest> items;
}
