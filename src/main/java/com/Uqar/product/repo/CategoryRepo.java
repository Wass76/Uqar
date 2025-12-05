package com.Uqar.product.repo;

import com.Uqar.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepo extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
    
    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.translations tr LEFT JOIN FETCH tr.language")
    List<Category> findAllWithTranslations();
    
    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.translations tr LEFT JOIN FETCH tr.language WHERE c.id = :id")
    Optional<Category> findByIdWithTranslations(@Param("id") Long id);
}
