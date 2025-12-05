package com.Uqar.utils.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

//@Entity
@Data
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
public class BaseEntity extends BaseIdEntity implements Serializable {
    @Override
    protected String getSequenceName() {
        throw new IllegalStateException("getSequenceName() must be overridden by the entity class");
    }
    // No need for ID field as it's inherited from BaseIdEntity
}
