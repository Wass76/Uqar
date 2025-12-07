package com.Uqar.product.repo;

import com.Uqar.product.entity.Manufacturer;
import com.Uqar.product.entity.ManufacturerTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManufacturerTranslationRepo extends JpaRepository<ManufacturerTranslation, Long> {
    boolean existsByName(String name);
    void deleteByManufacturer(Manufacturer manufacturer);
}
