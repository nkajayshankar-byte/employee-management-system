package com.EmployeeManagement.dao;

import com.EmployeeManagement.entity.Payslip;
import java.util.List;
import java.util.Optional;

public interface PayslipDAO {
    Payslip save(Payslip payslip);
    Optional<Payslip> findById(Long id);
    List<Payslip> findByEmployeeId(Long employeeId);
    Optional<Payslip> findByEmployeeIdAndMonthAndYear(Long employeeId, int month, int year);
    List<Payslip> findAll();
    void deleteById(Long id);
}
