package com.Uqar.product.repo;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.Uqar.product.entity.PharmacyProduct;
import com.Uqar.product.entity.PharmacyProductTranslation;

@Repository
public interface PharmacyProductTranslationRepo extends JpaRepository<PharmacyProductTranslation, Long> {
    void deleteByProduct(PharmacyProduct product);

    Set<PharmacyProductTranslation> findByProduct(PharmacyProduct product);
    
    // حذف الترجمات بواسطة product ID
    @Query("DELETE FROM PharmacyProductTranslation t WHERE t.product.id = :productId")
    @Modifying
    @Transactional
    void deleteByProductId(@Param("productId") Long productId);
    
    // البحث بواسطة product ID
    @Query("SELECT t FROM PharmacyProductTranslation t WHERE t.product.id = :productId")
    Set<PharmacyProductTranslation> findByProductId(@Param("productId") Long productId);
} 