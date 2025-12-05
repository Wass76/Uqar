package com.Uqar.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import com.Uqar.product.Enum.PaymentMethod;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Pay Customer Debts Request", example = """
{
  "totalPaymentAmount": 500.00,
  "paymentMethod": "CASH",
  "notes": "دفع عام للديون"
}
""")
public class PayCustomerDebtsRequest {
    
    @Schema(description = "Total payment amount to distribute across debts", example = "500.00")
    private BigDecimal totalPaymentAmount;
        
    @Schema(description = "Payment method", example = "CASH", allowableValues = {"CASH", "BANK_ACCOUNT"})
    private PaymentMethod paymentMethod;
    
    @Schema(description = "Payment notes", example = "دفع عام للديون")
    private String notes;
}
