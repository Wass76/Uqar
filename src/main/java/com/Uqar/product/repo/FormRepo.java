package com.Uqar.product.repo;

import com.Uqar.product.entity.Form;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FormRepo extends JpaRepository<Form, Long> {
    boolean existsByName(String name);
    
    @Query("SELECT DISTINCT f FROM Form f LEFT JOIN FETCH f.translations tr LEFT JOIN FETCH tr.language")
    List<Form> findAllWithTranslations();
    
    @Query("SELECT DISTINCT f FROM Form f LEFT JOIN FETCH f.translations tr LEFT JOIN FETCH tr.language WHERE f.id = :id")
    Optional<Form> findByIdWithTranslations(@Param("id") Long id);
}
