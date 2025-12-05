package com.Uqar.product.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Uqar.product.entity.PharmacyProduct;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyProductRepo extends JpaRepository<PharmacyProduct, Long> {
    Page<PharmacyProduct> findByPharmacyId(Long pharmacyId, Pageable pageable);
    
    @Query("SELECT DISTINCT p FROM PharmacyProduct p LEFT JOIN FETCH p.translations tr LEFT JOIN FETCH tr.language WHERE p.pharmacy.id = :pharmacyId")
    List<PharmacyProduct> findAllWithTranslations(@Param("pharmacyId") Long pharmacyId);
    
    @Query("SELECT DISTINCT p FROM PharmacyProduct p LEFT JOIN FETCH p.translations tr LEFT JOIN FETCH tr.language WHERE p.id = :id")
    Optional<PharmacyProduct> findByIdWithTranslations(@Param("id") Long id);
    
    @Query("""
    SELECT DISTINCT p FROM PharmacyProduct p
    LEFT JOIN p.translations pt
    LEFT JOIN p.barcodes pb
    WHERE (
        LOWER(p.tradeName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        LOWER(p.scientificName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        LOWER(pb.barcode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        (pt.language.code = :lang AND (
            LOWER(pt.tradeName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(pt.scientificName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ))
    )
    """)
    Page<PharmacyProduct> search(
            @Param("keyword") String keyword,
            @Param("lang") String lang,
            Pageable pageable);
    
    @Query("""
    SELECT DISTINCT p FROM PharmacyProduct p
    LEFT JOIN p.translations pt
    LEFT JOIN p.barcodes pb
    WHERE p.pharmacy.id = :pharmacyId AND (
        LOWER(p.tradeName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        LOWER(p.scientificName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        LOWER(pb.barcode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        (pt.language.code = :lang AND (
            LOWER(pt.tradeName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(pt.scientificName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ))
    )
    """)
    Page<PharmacyProduct> searchByPharmacyId(
            @Param("keyword") String keyword,
            @Param("lang") String lang,
            @Param("pharmacyId") Long pharmacyId,
            Pageable pageable);
    
    @Query("SELECT COUNT(pb) > 0 FROM PharmacyProductBarcode pb WHERE pb.barcode = :barcode")
    boolean existsByBarcode(@Param("barcode") String barcode);
    
    @Query("SELECT COUNT(pb) > 0 FROM PharmacyProductBarcode pb JOIN pb.product p WHERE pb.barcode = :barcode AND p.pharmacy.id = :pharmacyId")
    boolean existsByBarcodeAndPharmacyId(@Param("barcode") String barcode, @Param("pharmacyId") Long pharmacyId);
    
    @Query("SELECT DISTINCT p FROM PharmacyProduct p LEFT JOIN FETCH p.translations tr LEFT JOIN FETCH tr.language WHERE p.id = :id AND p.pharmacy.id = :pharmacyId")
    Optional<PharmacyProduct> findByIdAndPharmacyIdWithTranslations(@Param("id") Long id, @Param("pharmacyId") Long pharmacyId);
    
    @Query("SELECT COUNT(p) > 0 FROM PharmacyProduct p WHERE p.id = :id AND p.pharmacy.id = :pharmacyId")
    boolean existsByIdAndPharmacyId(@Param("id") Long id, @Param("pharmacyId") Long pharmacyId);
    
    boolean existsByTypeId(Long typeId);
}
