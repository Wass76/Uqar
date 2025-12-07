package com.Uqar.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.Uqar.utils.entity.AuditedEntity;

// import lombok.EqualsAndHashCode;
// import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=false)
@Entity
@Table(
        name = "pharmacy_product_barcode",
        indexes = {
                @Index(columnList = "barcode", unique = true)
        })
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyProductBarcode extends AuditedEntity {

    @Column(nullable = false, unique = true)
    private String barcode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
   @EqualsAndHashCode.Exclude
   @ToString.Exclude
    private PharmacyProduct product;

    @Override
    protected String getSequenceName() {
        return "pharmacy_product_barcode_id_seq";
    }
} 