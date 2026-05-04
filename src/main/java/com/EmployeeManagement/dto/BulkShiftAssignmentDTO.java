package com.EmployeeManagement.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class BulkShiftAssignmentDTO {
    private List<String> employeeIds;
    private String shiftId;
    private LocalDate startDate;
    private LocalDate endDate;
}
