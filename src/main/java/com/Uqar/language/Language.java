package com.Uqar.language;

import com.Uqar.utils.entity.AuditedEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@EqualsAndHashCode(callSuper=false)
@Table(name = "languages")
@NoArgsConstructor
@AllArgsConstructor
public class Language extends AuditedEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    String code;
    String name;

    public Language(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
