package com.EmployeeManagement.dao;

import com.EmployeeManagement.entity.Shift;
import java.util.List;
import java.util.Optional;

public interface ShiftDAO {
    Shift save(Shift shift);
    Optional<Shift> findById(Long id);
    List<Shift> findAll();
    void deleteById(Long id);
}
