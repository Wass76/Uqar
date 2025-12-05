package com.Uqar.product.repo;

import com.Uqar.product.entity.Manufacturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ManufacturerRepo extends JpaRepository<Manufacturer, Long> {
    boolean existsByName(String name);
    
    @Query("SELECT DISTINCT m FROM Manufacturer m LEFT JOIN FETCH m.translations tr LEFT JOIN FETCH tr.language")
    List<Manufacturer> findAllWithTranslations();
    
    @Query("SELECT DISTINCT m FROM Manufacturer m LEFT JOIN FETCH m.translations tr LEFT JOIN FETCH tr.language WHERE m.id = :id")
    Optional<Manufacturer> findByIdWithTranslations(@Param("id") Long id);
}
