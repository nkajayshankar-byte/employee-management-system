package com.EmployeeManagement.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobApplication {
    private String id;
    private String jobId;
    private String employeeId;
    private String employeeName;
    private String employeeEmail;
    private String resumeUrl;
    private String status = "PENDING";
    @JsonFormat(pattern = "dd-MM-yy HH:mm", timezone = "Asia/Kolkata")
    private LocalDateTime appliedDate = LocalDateTime.now();
}
