package com.EmployeeManagement.dao;

import com.EmployeeManagement.entity.Company;
import java.util.List;
import java.util.Optional;

public interface CompanyDAO {
    Company save(Company company);
    List<Company> findAll();
    Optional<Company> findById(String id);
}
