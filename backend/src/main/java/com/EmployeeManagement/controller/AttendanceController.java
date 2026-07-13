package com.EmployeeManagement.controller;

import com.EmployeeManagement.entity.Attendance;
import com.EmployeeManagement.service.AttendanceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yy");

    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn(@RequestParam Long employeeId) {
        try {
            return ResponseEntity.ok(attendanceService.checkIn(employeeId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/check-out")
    public ResponseEntity<?> checkOut(@RequestParam Long employeeId) {
        try {
            return ResponseEntity.ok(attendanceService.checkOut(employeeId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/employee/{id}")
    public ResponseEntity<List<Attendance>> getEmployeeAttendance(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getEmployeeAttendance(id));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<Attendance>> getAttendanceByDate(
            @PathVariable @DateTimeFormat(pattern = "dd-MM-yy") LocalDate date) {
        return ResponseEntity.ok(attendanceService.getAttendanceByDate(date));
    }

    @GetMapping("/range")
    public ResponseEntity<List<Attendance>> getAttendanceByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate, DATE_FMT);
        LocalDate end = LocalDate.parse(endDate, DATE_FMT);
        return ResponseEntity.ok(attendanceService.getAttendanceByDateRange(start, end));
    }
}
