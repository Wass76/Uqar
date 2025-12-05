package com.Uqar.product.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Uqar.product.entity.PharmacyProductBarcode;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyProductBarcodeRepo extends JpaRepository<PharmacyProductBarcode, Long> {
    
    Optional<PharmacyProductBarcode> findByBarcode(String barcode);
    
    boolean existsByBarcode(String barcode);
    
    List<PharmacyProductBarcode> findByProductId(Long productId);
    
    @Query("SELECT pb FROM PharmacyProductBarcode pb WHERE pb.barcode IN :barcodes")
    List<PharmacyProductBarcode> findByBarcodes(@Param("barcodes") List<String> barcodes);
    
    @Query("SELECT COUNT(pb) > 0 FROM PharmacyProductBarcode pb WHERE pb.barcode = :barcode AND pb.product.id != :productId")
    boolean existsByBarcodeAndProductIdNot(@Param("barcode") String barcode, @Param("productId") Long productId);
    
    @Query("SELECT COUNT(pb) > 0 FROM PharmacyProductBarcode pb JOIN pb.product p WHERE pb.barcode = :barcode AND pb.product.id != :productId AND p.pharmacy.id = :pharmacyId")
    boolean existsByBarcodeAndProductIdNotAndPharmacyId(@Param("barcode") String barcode, @Param("productId") Long productId, @Param("pharmacyId") Long pharmacyId);
} 