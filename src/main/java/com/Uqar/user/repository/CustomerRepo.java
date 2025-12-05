package com.Uqar.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Uqar.user.entity.Customer;

@Repository
public interface CustomerRepo extends JpaRepository<Customer, Long> {
    Optional<Customer> findByName(String name);
    
    // دوال البحث حسب الصيدلية
    List<Customer> findByPharmacyId(Long pharmacyId);
    
    Optional<Customer> findByIdAndPharmacyId(Long id, Long pharmacyId);
    
    Optional<Customer> findByNameAndPharmacyId(String name, Long pharmacyId);
    
    List<Customer> findByNameContainingIgnoreCaseAndPharmacyId(String name, Long pharmacyId);
} 