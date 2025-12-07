package com.Uqar.user.entity;

import com.Uqar.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "area_translations")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AreaTranslation extends AuditedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    private String translatedName;

    @Column
    private String translatedDescription;

    @Override
    protected String getSequenceName() {
        return "area_translation_id_seq";
    }
}
