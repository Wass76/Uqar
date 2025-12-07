package com.Uqar.user.repository;

import com.Uqar.user.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
    List<Employee> findByPharmacy_Id(Long pharmacyId);
    
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.pharmacy WHERE e.email = :email")
    Optional<Employee> findByEmailWithPharmacy(@Param("email") String email);
} 