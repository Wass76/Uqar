package com.Uqar.product.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
// import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

// import lombok.ToString;
import com.Uqar.utils.entity.AuditedEntity;


import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(callSuper=false)
@Table(name = "categories")
@NoArgsConstructor
@AllArgsConstructor
public class Category extends AuditedEntity{


    @Column(nullable = false)
    private String name;


    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<MasterProduct> products = new HashSet<>();

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    // @Fetch(FetchMode.SUBSELECT)
    private Set<CategoryTranslation> translations = new HashSet<>();

    @Override
    protected String getSequenceName() {
        return "category_id_seq";
    }


}

