package com.EmployeeManagement.controller;

import com.EmployeeManagement.dto.PayslipDto;
import com.EmployeeManagement.dto.SalaryStructureDto;
import com.EmployeeManagement.service.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
@CrossOrigin("*")
public class PayrollController {

    @Autowired
    private PayrollService payrollService;

    @PostMapping("/salary")
    public ResponseEntity<SalaryStructureDto> saveSalaryStructure(@RequestBody SalaryStructureDto dto) {
        return ResponseEntity.ok(payrollService.saveSalaryStructure(dto));
    }

    @GetMapping("/salary/{employeeId}")
    public ResponseEntity<SalaryStructureDto> getSalaryStructure(@PathVariable Long employeeId) {
        SalaryStructureDto dto = payrollService.getSalaryStructure(employeeId);
        if (dto != null) {
            return ResponseEntity.ok(dto);
        }
        // Return a default empty structure instead of 404
        SalaryStructureDto empty = new SalaryStructureDto();
        empty.setEmployeeId(employeeId);
        empty.setBaseSalary(0.0);
        empty.setHra(0.0);
        empty.setOtherAllowances(0.0);
        empty.setTaxDeductions(0.0);
        empty.setProvidentFund(0.0);
        empty.setNetSalary(0.0);
        return ResponseEntity.ok(empty);
    }

    @PostMapping("/generate/{employeeId}")
    public ResponseEntity<PayslipDto> generatePayslip(
            @PathVariable Long employeeId,
            @RequestParam int month,
            @RequestParam int year) {
        try {
            return ResponseEntity.ok(payrollService.generatePayslip(employeeId, month, year));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/payslips/{employeeId}")
    public ResponseEntity<List<PayslipDto>> getEmployeePayslips(@PathVariable Long employeeId) {
        return ResponseEntity.ok(payrollService.getEmployeePayslips(employeeId));
    }
}
