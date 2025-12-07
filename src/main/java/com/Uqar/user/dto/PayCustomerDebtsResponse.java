package com.Uqar.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Pay Customer Debts Response")
public class PayCustomerDebtsResponse {
    
    @Schema(description = "Customer ID", example = "1")
    private Long customerId;
    
    @Schema(description = "Customer name", example = "أحمد محمد")
    private String customerName;
    
    @Schema(description = "Total payment amount", example = "500.00")
    private Float totalPaymentAmount;
    
    @Schema(description = "Total remaining debt after payment", example = "200.00")
    private Float totalRemainingDebt;
    
    @Schema(description = "List of debts that received payments")
    private List<DebtPaymentDetail> debtPayments;
    
    @Schema(description = "Payment notes", example = "دفع عام للديون")
    private String notes;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DebtPaymentDetail {
        
        @Schema(description = "Debt ID", example = "1")
        private Long debtId;
        
        @Schema(description = "Original debt amount", example = "300.00")
        private Float originalAmount;
        
        @Schema(description = "Amount paid to this debt", example = "200.00")
        private Float amountPaid;
        
        @Schema(description = "Remaining amount after payment", example = "100.00")
        private Float remainingAmount;
        
        @Schema(description = "Debt status after payment", example = "ACTIVE")
        private String status;
        
        @Schema(description = "Debt due date", example = "2024-12-31")
        private String dueDate;
    }
}
