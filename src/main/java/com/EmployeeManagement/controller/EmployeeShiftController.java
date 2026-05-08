package com.EmployeeManagement.controller;

import com.EmployeeManagement.dto.BulkShiftAssignmentDTO;
import com.EmployeeManagement.entity.EmployeeShift;
import com.EmployeeManagement.service.EmployeeShiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shift-assign")
public class EmployeeShiftController {

    @Autowired
    private EmployeeShiftService employeeShiftService;

    @PostMapping
    public ResponseEntity<EmployeeShift> assignShift(@RequestBody EmployeeShift employeeShift) {
        return ResponseEntity.ok(employeeShiftService.assignShift(employeeShift));
    }

    @PostMapping("/bulk")
    public ResponseEntity<Void> bulkAssign(@RequestBody BulkShiftAssignmentDTO bulkDto) {
        employeeShiftService.bulkAssign(
            bulkDto.getEmployeeIds(), 
            bulkDto.getShiftId(), 
            bulkDto.getStartDate(), 
            bulkDto.getEndDate()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/employee/{id}")
    public ResponseEntity<List<EmployeeShift>> getEmployeeShifts(@PathVariable Long id) {
        return ResponseEntity.ok(employeeShiftService.getEmployeeShifts(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<EmployeeShift>> getAllAssignments() {
        return ResponseEntity.ok(employeeShiftService.getAllAssignments());
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<EmployeeShift>> getAssignmentsByDate(
            @PathVariable @DateTimeFormat(pattern = "dd-MM-yy") LocalDate date) {
        return ResponseEntity.ok(employeeShiftService.getAssignmentsByDate(date));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        employeeShiftService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<Void> bulkDelete(@RequestBody List<Long> ids) {
        employeeShiftService.bulkDelete(ids);
        return ResponseEntity.ok().build();
    }
}
