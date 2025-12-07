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
@Table(name = "form_translation")
@NoArgsConstructor
@AllArgsConstructor
public class FormTranslation extends AuditedEntity {
    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Form form;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id")
    private Language language;

    @Override
    protected String getSequenceName() {
        return "form_translation_id_seq";
    }
}
