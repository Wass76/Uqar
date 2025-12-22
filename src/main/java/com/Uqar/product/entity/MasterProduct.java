package com.Uqar.product.entity;

import java.util.HashSet;
import java.util.Set;

import com.Uqar.utils.entity.AuditedEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
   
    @Column(name = "number_of_parts_per_box")
    private Integer numberOfPartsPerBox;

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
