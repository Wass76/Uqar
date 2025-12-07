package com.Uqar.sale.entity;

import jakarta.persistence.*;
import jakarta.persistence.EnumType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

import com.Uqar.sale.enums.RefundStatus;
import com.Uqar.user.entity.Pharmacy;
import com.Uqar.utils.entity.AuditedEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sale_refunds")
public class SaleRefund extends AuditedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_invoice_id", nullable = false)
    private SaleInvoice saleInvoice;

    @Column(nullable = false)
    private Float totalRefundAmount;

    @Column(length = 500)
    private String refundReason;

    @JsonFormat(pattern = "dd-MM-yyyy'T'HH:mm:ss")
    @Column(nullable = false)
    private LocalDateTime refundDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus refundStatus = RefundStatus.NO_REFUND;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;

    @OneToMany(mappedBy = "saleRefund", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleRefundItem> refundItems;

    @Column(nullable = false)
    private Boolean stockRestored = false;

    @Override
    protected String getSequenceName() {
        return "sale_refund_id_seq";
    }
}
