package com.EmployeeManagement.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class BulkShiftAssignmentDTO {
    private List<Long> employeeIds;
    private Long shiftId;
    private LocalDate startDate;
    private LocalDate endDate;
}
