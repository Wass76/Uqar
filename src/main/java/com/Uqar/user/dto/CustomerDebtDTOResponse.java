package com.Uqar.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.Uqar.product.Enum.PaymentMethod;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerDebtDTOResponse {
    private Long id;
    private Long customerId;
    
    @Schema(description = "اسم العميل", example = "cash customer")
    private String customerName;
    private Long pharmacyId;
    
    private Float amount;
    

    private Float paidAmount;
    
    private Float remainingAmount;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
    
    private String notes;
    private String status;
    

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate paidAt;
    
    @Schema(description = "طريقة الدفع المستخدمة", example = "CASH")
    private PaymentMethod paymentMethod;
} 