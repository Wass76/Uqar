package com.Uqar.product.repo;

import com.Uqar.product.entity.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TypeRepo extends JpaRepository<Type, Long> {
    boolean existsByName(String name);
    
    @Query("SELECT DISTINCT t FROM Type t LEFT JOIN FETCH t.translations tr LEFT JOIN FETCH tr.language")
    List<Type> findAllWithTranslations();
    
    @Query("SELECT DISTINCT t FROM Type t LEFT JOIN FETCH t.translations tr LEFT JOIN FETCH tr.language WHERE t.id = :id")
    Optional<Type> findByIdWithTranslations(@Param("id") Long id);
}
