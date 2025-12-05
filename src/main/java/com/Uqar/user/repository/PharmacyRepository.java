package com.Uqar.user.repository;

import com.Uqar.user.entity.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
@Repository
public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {
    boolean existsByLicenseNumber(String licenseNumber);
} 