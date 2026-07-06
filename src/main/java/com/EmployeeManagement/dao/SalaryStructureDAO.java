package com.EmployeeManagement.dao;

import com.EmployeeManagement.entity.SalaryStructure;
import java.util.List;
import java.util.Optional;

public interface SalaryStructureDAO {
    SalaryStructure save(SalaryStructure salaryStructure);
    Optional<SalaryStructure> findById(Long id);
    Optional<SalaryStructure> findByEmployeeId(Long employeeId);
    List<SalaryStructure> findAll();
    void deleteById(Long id);
}
