package com.Uqar.sale.dto;

import com.Uqar.product.Enum.DiscountType;
import com.Uqar.product.Enum.PaymentType;
import com.Uqar.product.Enum.PaymentMethod;
import com.Uqar.sale.enums.PaymentStatus;
import com.Uqar.sale.enums.RefundStatus;
import com.Uqar.user.Enum.Currency;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SaleInvoiceDTOResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    
    @JsonFormat(pattern = "dd-MM-yyyy, HH:mm:ss")
    private LocalDateTime invoiceDate;
    
    private float totalAmount;
    private PaymentType paymentType;
    private PaymentMethod paymentMethod;
    private Currency currency;
    private float discount;
    private DiscountType discountType;
    private float paidAmount;
    private float remainingAmount;
    //private InvoiceStatus status;
    private PaymentStatus paymentStatus;
    private RefundStatus refundStatus;
    private List<SaleInvoiceItemDTOResponse> items;
}
