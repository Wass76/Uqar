package com.Uqar.user.dto;

import com.Uqar.utils.annotation.ValidPassword;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.Uqar.user.Enum.UserStatus;
import com.Uqar.utils.annotation.ValidEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Schema(description = "Request to create a new employee")
public class EmployeeCreateRequestDTO {
    @Schema(description = "Employee's first name", example = "John")
    private String firstName;
    
    @Schema(description = "Employee's last name", example = "Doe")
    private String lastName;
    
    @Schema(description = "Employee's password", example = "Password!1")
    @ValidPassword
    private String password;
    
    @Schema(description = "Employee's phone number", example = "1234567890")
    private String phoneNumber;
    
//    @ValidEnum(enumClass = UserStatus.class)
    @Schema(description = "Employee's status", example = "ACTIVE")
    private UserStatus status;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Employee's hire date", example = "2024-01-15")
    private LocalDate dateOfHire;
    
    @Schema(description = "Role ID for the employee", example = "2")
    private Long roleId;
    
    @Schema(description = "Working hours requests for the employee")
    private List<CreateWorkingHoursRequestDTO> workingHoursRequests; // New easier format
} 