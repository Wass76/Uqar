package com.Uqar.user.entity;

import com.Uqar.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Entity
@Table(name = "areas")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Area extends AuditedEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    private String arabicName;

    @Column
    private Boolean isActive;

    @OneToMany(mappedBy = "area")
    private Set<Pharmacy> pharmacies;

    @OneToMany(mappedBy = "area", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<AreaTranslation> translations;

    @Override
    protected String getSequenceName() {
        return "area_id_seq";
    }
}
