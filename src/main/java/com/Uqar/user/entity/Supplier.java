package com.Uqar.user.entity;

import com.Uqar.user.Enum.Currency;
import com.Uqar.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "suppliers", uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "pharmacy_id"})})
@NoArgsConstructor
@AllArgsConstructor
public class Supplier extends AuditedEntity {
    @Column(nullable = false)
    private String name;

    @Column
    private String phone;

    @Column
    private String address;

    @Column
    private Currency preferredCurrency; // SYP or USD

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;

    @Override
    protected String getSequenceName() {
        return "supplier_id_seq";
    }
} 