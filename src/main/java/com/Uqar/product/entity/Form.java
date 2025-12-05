package com.Uqar.product.entity;

import com.Uqar.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
// import org.hibernate.annotations.Fetch;
// import org.hibernate.annotations.FetchMode;

import java.util.HashSet;
import java.util.Set;


@Entity
@Data
@EqualsAndHashCode(callSuper=false)
@Table(name = "forms")
@NoArgsConstructor
@AllArgsConstructor
public class Form extends AuditedEntity {

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "form")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<MasterProduct> product;

    @OneToMany(mappedBy = "form", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
   // @Fetch(FetchMode.SUBSELECT)
    private Set<FormTranslation> translations = new HashSet<>();

    @Override
    protected String getSequenceName() {
        return "form_id_seq";
    }
}
