package com.Uqar.user.repository;

import com.Uqar.user.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByPharmacyId(Long pharmacyId);
    List<Supplier> findByPharmacyIdAndNameContainingIgnoreCase(Long pharmacyId, String name);
    boolean existsByNameAndPharmacyId(String name, Long pharmacyId);
    boolean existsByNameAndPharmacyIdAndIdNot(String name, Long pharmacyId, Long id);
} 