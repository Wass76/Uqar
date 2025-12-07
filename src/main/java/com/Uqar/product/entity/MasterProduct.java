package com.Uqar.product.entity;

import com.Uqar.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;
import java.util.HashSet;

@Data
@EqualsAndHashCode(callSuper=false)
@Entity
@Table(
        name = "master_product",
        indexes = {
                @Index(columnList = "barcode")
        })
@NoArgsConstructor
@AllArgsConstructor
public class MasterProduct extends AuditedEntity {


    private String tradeName;
    private String scientificName;
    private String concentration;
    private String size;
    private float refPurchasePrice;
    private float refSellingPrice;
    private Integer minStockLevel;
    private String notes;
    private float tax;

    @Column(nullable = false, unique = true , name = "barcode")
    private String barcode;
    private Boolean requiresPrescription;

    @ManyToMany(cascade = {CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_category",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Category> categories = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "type_id")
    private Type type;

    @ManyToOne
    @JoinColumn(name = "form_id")
    private Form form;

    @ManyToOne
    @JoinColumn(name = "manufacturer_id")
    private Manufacturer manufacturer;


    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<MasterProductTranslation> translations = new HashSet<>();

    @Override
    protected String getSequenceName() {
        return "master_product_id_seq";
    }


}
