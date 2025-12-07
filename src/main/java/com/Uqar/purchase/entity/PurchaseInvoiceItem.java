package com.Uqar.purchase.entity;

import com.Uqar.product.Enum.ProductType;
import com.Uqar.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "purchase_invoice_item")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PurchaseInvoiceItem extends AuditedEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_invoice_id", nullable = false)
    private PurchaseInvoice purchaseInvoice;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private ProductType productType; // 'MASTER' or 'PHARMACY'

    @Column(nullable = false)
    private Integer receivedQty;

    @Column(nullable = false)
    private Integer bonusQty;

    @Column(nullable = false)
    private Double invoicePrice;

    @Column(nullable = false)
    private Double actualPrice;

    @Column(nullable = true)
    private String batchNo;

    @Column(nullable = true)
    private String invoiceNumber;

    @Column(nullable = true)
    private LocalDate expiryDate;

    @EqualsAndHashCode.Include
    protected Long getIdForEquals() {
        return super.getId();
    }

    @Override
    protected String getSequenceName() {
        return "purchase_invoice_item_id_seq";
    }
} 