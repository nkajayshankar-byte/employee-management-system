package com.EmployeeManagement.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Attendance {
    private String id;
    private String employeeId;

    @JsonFormat(pattern = "dd-MM-yy")
    private LocalDate date;

    @JsonFormat(pattern = "dd-MM-yy HH:mm", timezone = "Asia/Kolkata")
    private LocalDateTime checkInTime;

    @JsonFormat(pattern = "dd-MM-yy HH:mm", timezone = "Asia/Kolkata")
    private LocalDateTime checkOutTime;

    private String status; // Present, Absent, Late
    private Double workingHours;
}

