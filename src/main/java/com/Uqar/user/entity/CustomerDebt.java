package com.Uqar.user.entity;

import com.Uqar.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import com.Uqar.product.Enum.PaymentMethod;

@Entity
@Data
@EqualsAndHashCode(callSuper=false)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer_debt")
public class CustomerDebt extends AuditedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private Float amount; 

    @Column(nullable = false)
    private Float paidAmount;

    @Column(nullable = false)
    private Float remainingAmount; 

    @Column(nullable = false)
    private LocalDate dueDate; 

    @Column
    private String notes;

    @Column(nullable = false)
    private String status; // ACTIVE, PAID, OVERDUE

    @Column
    private LocalDate paidAt;
    
    @Enumerated(EnumType.STRING)
    @Column
    private PaymentMethod paymentMethod;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = "ACTIVE";
        }
    }

    @Override
    protected String getSequenceName() {
        return "customer_debt_id_seq";
    }
} 