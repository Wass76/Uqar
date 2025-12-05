package com.Uqar.sale.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import com.Uqar.utils.entity.AuditedEntity;

@Entity
@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sale_refund_items")
public class SaleRefundItem extends AuditedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_refund_id", nullable = false)
    private SaleRefund saleRefund;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_invoice_item_id", nullable = false)
    private SaleInvoiceItem saleInvoiceItem;

    @Column(nullable = false)
    private Integer refundQuantity;

    @Column(nullable = false)
    private Float unitPrice;

    @Column(nullable = false)
    private Float subtotal;

    @Column(length = 500)
    private String itemRefundReason;

    @Column(nullable = false)
    private Boolean stockRestored = false;

    @Override
    protected String getSequenceName() {
        return "sale_refund_item_id_seq";
    }
}
