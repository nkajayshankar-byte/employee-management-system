package com.EmployeeManagement.service;

import com.EmployeeManagement.entity.EmployeeShift;
import com.EmployeeManagement.dao.EmployeeShiftDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmployeeShiftService {

    @Autowired
    private EmployeeShiftDAO employeeShiftDAO;

    public EmployeeShift assignShift(EmployeeShift employeeShift) {
        return employeeShiftDAO.save(employeeShift);
    }

    public List<EmployeeShift> getEmployeeShifts(Long employeeId) {
        return employeeShiftDAO.findByEmployeeId(employeeId);
    }

    public List<EmployeeShift> getAssignmentsByDate(LocalDate date) {
        return employeeShiftDAO.findAssignmentsForDate(date);
    }

    public List<EmployeeShift> getAllAssignments() {
        return employeeShiftDAO.findAll();
    }

    public EmployeeShift getShiftForEmployeeOnDate(Long employeeId, LocalDate date) {
        return employeeShiftDAO.findByEmployeeIdAndDate(employeeId, date).orElse(null);
    }

    public void deleteAssignment(Long id) {
        employeeShiftDAO.deleteById(id);
    }

    public void bulkDelete(List<Long> ids) {
        for (Long id : ids) {
            employeeShiftDAO.deleteById(id);
        }
    }

    public void bulkAssign(List<Long> employeeIds, Long shiftId, LocalDate startDate, LocalDate endDate) {
        for (Long empId : employeeIds) {
            List<EmployeeShift> existing = employeeShiftDAO.findByEmployeeId(empId);
            for (EmployeeShift ex : existing) {
                // Overlap check: (StartA <= EndB) and (EndA >= StartB)
                if (!ex.getStartDate().isAfter(endDate) && !ex.getEndDate().isBefore(startDate)) {
                    employeeShiftDAO.deleteById(ex.getId());
                }
            }

            EmployeeShift assignment = new EmployeeShift();
            assignment.setEmployeeId(empId);
            assignment.setShiftId(shiftId);
            assignment.setStartDate(startDate);
            assignment.setEndDate(endDate);
            employeeShiftDAO.save(assignment);
        }
    }
}
