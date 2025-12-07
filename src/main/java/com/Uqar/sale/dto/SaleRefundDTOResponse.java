package com.Uqar.sale.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import com.Uqar.sale.enums.RefundStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Sale Refund Response")
public class SaleRefundDTOResponse {
    
    @Schema(description = "Refund ID", example = "1")
    private Long refundId;
    
    @Schema(description = "Sale invoice ID", example = "1")
    private Long saleInvoiceId;
    
    @Schema(description = "Total refund amount", example = "150.0")
    private Float totalRefundAmount;
    
    @Schema(description = "Refund reason", example = "Customer request")
    private String refundReason;
    
    @Schema(description = "Refund date", example = "2024-01-15T10:30:00")
    @JsonFormat(pattern = "dd-MM-yyyy, HH:mm:ss")
    private LocalDateTime refundDate;

    @Schema(description = "Refund status", example = "CREDIT")
    private RefundStatus refundStatus;
    
    @Schema(description = "Refunded items")
    private List<SaleRefundItemDTOResponse> refundedItems;
    
    @Schema(description = "Stock restored", example = "true")
    private Boolean stockRestored;
    
    // معلومات العميل
    @Schema(description = "Customer ID", example = "1")
    private Long customerId;
    
    @Schema(description = "Customer name", example = "أحمد محمد")
    private String customerName;
    
    // معلومات الفاتورة الأصلية
    @Schema(description = "Original invoice total amount", example = "500.0")
    private Float originalInvoiceAmount;
    
    @Schema(description = "Original invoice paid amount", example = "200.0")
    private Float originalInvoicePaidAmount;
    
    @Schema(description = "Original invoice remaining amount", example = "300.0")
    private Float originalInvoiceRemainingAmount;
    
    @Schema(description = "Payment type", example = "CREDIT")
    private String paymentType;
    
    @Schema(description = "Payment method", example = "BANK_ACCOUNT")
    private String paymentMethod;
    
    @Schema(description = "Currency", example = "SYP")
    private String currency;
    
    // معلومات الدين الحالي للعميل
    @Schema(description = "Customer total current debt", example = "500.0")
    private Float customerTotalDebt;
    
    @Schema(description = "Number of active debts for customer", example = "2")
    private Integer customerActiveDebtsCount;
    

  
 
}
