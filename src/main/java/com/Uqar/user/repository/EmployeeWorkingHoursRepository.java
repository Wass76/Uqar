package com.Uqar.user.repository;

import com.Uqar.user.entity.EmployeeWorkingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeWorkingHoursRepository extends JpaRepository<EmployeeWorkingHours, Long> {
    List<EmployeeWorkingHours> findByEmployee_Id(Long employeeId);
    Optional<EmployeeWorkingHours> findByEmployee_IdAndDayOfWeek(Long employeeId, DayOfWeek dayOfWeek);
    List<EmployeeWorkingHours> findByEmployee_IdAndDayOfWeekIn(Long employeeId, List<DayOfWeek> daysOfWeek);
    void deleteByEmployee_Id(Long employeeId);
    void deleteByEmployee_IdAndDayOfWeek(Long employeeId, DayOfWeek dayOfWeek);
} 