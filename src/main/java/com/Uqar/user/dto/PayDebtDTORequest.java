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
@Schema(description = "Pay Debt Request", example = """
{
  "debtId": 1,
  "paymentAmount": 100.00,
  "paymentMethod": "CASH",
  "notes": "Partial payment"
}
""")
public class PayDebtDTORequest {
    
    @Schema(description = "Debt ID to pay", example = "1")
    private Long debtId;
    @Schema(description = "Payment amount", example = "100.00")
    private BigDecimal paymentAmount;
    @Schema(description = "Payment method", example = "CASH")
    private PaymentMethod paymentMethod;
    @Schema(description = "Payment notes", example = "Partial payment")
    private String notes;
} 