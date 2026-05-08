package com.EmployeeManagement.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeShift {
    private Long id;
    private Long employeeId;
    private Long shiftId;

    @JsonFormat(pattern = "dd-MM-yy")
    private LocalDate startDate;

    @JsonFormat(pattern = "dd-MM-yy")
    private LocalDate endDate;
}
