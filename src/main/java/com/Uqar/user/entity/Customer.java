package com.Uqar.user.entity;

import java.util.List;

import com.Uqar.utils.entity.AuditedEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@EqualsAndHashCode(callSuper=false)
@Table(name = "customers")
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends AuditedEntity{

    private String name = "cash customer";

    @Column(nullable = true)
    private String phoneNumber;

    @Column(nullable = true)
    private String address;

    @Column(nullable = true)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CustomerDebt> debts;

    //  @Transient 
    // public boolean isDebit() {
    // return this.debt != null && this.debt > 0;
//} 
   // private Float debt;

    @Override
    protected String getSequenceName() {
        return "customer_id_seq";
    }

}
