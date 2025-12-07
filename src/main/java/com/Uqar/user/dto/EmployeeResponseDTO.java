package com.Uqar.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.Uqar.user.Enum.UserStatus;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class EmployeeResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private UserStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfHire;
    private String roleName;
    private Long pharmacyId;
    private List<EmployeeWorkingHoursDTO> workingHours; // New flexible working hours
} 