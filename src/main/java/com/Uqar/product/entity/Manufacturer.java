package com.Uqar.product.entity;


import com.Uqar.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(callSuper=false)
@Table(name = "manufacturers")
@NoArgsConstructor
@AllArgsConstructor
public class Manufacturer extends AuditedEntity {

    @Column(nullable = false)
    private String name;


    @OneToMany(mappedBy = "manufacturer")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<MasterProduct> product;

    @OneToMany(mappedBy = "manufacturer", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
   // @Fetch(FetchMode.SUBSELECT)
    private Set<ManufacturerTranslation> translations = new HashSet<>();

    @Override
    protected String getSequenceName() {
        return "manufacturer_id_seq";
    }

}

