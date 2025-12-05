package com.Uqar.product.entity;


import com.Uqar.language.Language;
import com.Uqar.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Data
@EqualsAndHashCode(callSuper=false)
@Table(name = "master_product_translation")
@NoArgsConstructor
@AllArgsConstructor
public class MasterProductTranslation extends AuditedEntity {
    private String tradeName;
    private String scientificName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private MasterProduct product;

    @ManyToOne
    @JoinColumn(name = "language_id")
    private Language language;

    @Override
    protected String getSequenceName() {
        return "master_product_translation_id_seq";
    }
}
