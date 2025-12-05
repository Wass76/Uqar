package com.Uqar.user.dto;

import lombok.Data;
import java.time.DayOfWeek;
import java.util.List;

@Data
public class EmployeeWorkingHoursDTO {
    private DayOfWeek dayOfWeek;
    private List<WorkShiftDTO> shifts;
} 