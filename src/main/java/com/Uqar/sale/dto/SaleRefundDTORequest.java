package com.Uqar.sale.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Sale Refund Request", example = """
{
    "refundItems": [
      {
        "itemId": 15,
        "quantity": 2,
        "itemRefundReason": "العميل طلب إرجاع"
      }
    ],
    "refundReason": "إرجاع طلب العميل"
}
""")
public class SaleRefundDTORequest {
    
    @Schema(description = "List of items to refund", example = "[]")
    @NotNull(message = "Refund items list is required")
    @Size(min = 1, message = "At least one item must be refunded")
    private List<SaleRefundItemDTORequest> refundItems;
    
    @Schema(description = "Reason for refund", example = "Customer request")
    private String refundReason;
    
   
}
