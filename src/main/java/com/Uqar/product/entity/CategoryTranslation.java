package com.Uqar.product.entity;


import com.Uqar.language.Language;
import com.Uqar.utils.entity.AuditedEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@EqualsAndHashCode(callSuper=false)
@Table(name = "Category_Translation")
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTranslation extends AuditedEntity{

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id")
    private Language language;

    @Override
    protected String getSequenceName() {
        return "category_translation_id_seq";
    }
}
