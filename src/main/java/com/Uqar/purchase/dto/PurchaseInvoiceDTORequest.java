package com.Uqar.purchase.dto;

import com.Uqar.user.Enum.Currency;
import com.Uqar.product.Enum.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "Purchase Invoice Request", example = """
{
  "purchaseOrderId": 1,
  "supplierId": 1,
  "currency": "USD",
  "total": 550.00,
  "invoiceNumber":"123456",
  "items": [
    {
      "productId": 1,
      "quantity": 100,
      "unitPrice": 5.50
    }
  ]
}
""")
public class PurchaseInvoiceDTORequest {
    
    @Schema(description = "Purchase order ID", example = "1")
    private Long purchaseOrderId;
    
    @Schema(description = "Supplier ID", example = "1")
    private Long supplierId;
    
    @Schema(description = "Currency for the invoice", example = "USD", 
            allowableValues = {"USD", "EUR", "SYP"})
    private Currency currency;
    
    @Schema(description = "Total invoice amount", example = "550.00")
    private Double total;
    
    @Schema(description = "Invoice number (optional)", example = "INV-2024-001")
    private String invoiceNumber;
    
    // Payment method is optional and defaults to CASH if not specified
    @Schema(description = "Payment method (optional, defaults to CASH)", example = "CASH",
            allowableValues = {"CASH", "BANK_ACCOUNT", "CHECK"})
    private PaymentMethod paymentMethod = PaymentMethod.CASH;
    
    @Schema(description = "List of invoice items")
    private List<PurchaseInvoiceItemDTORequest> items;
} 