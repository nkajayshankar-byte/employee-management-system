package com.EmployeeManagement.service;

import com.EmployeeManagement.entity.Shift;
import com.EmployeeManagement.dao.ShiftDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShiftService {

    @Autowired
    private ShiftDAO shiftDAO;

    public Shift createShift(Shift shift) {
        return shiftDAO.save(shift);
    }

    public List<Shift> getAllShifts() {
        return shiftDAO.findAll();
    }

    public Shift updateShift(Long id, Shift shiftDetails) {
        Shift shift = shiftDAO.findById(id).orElseThrow(() -> new RuntimeException("Shift not found"));
        shift.setShiftName(shiftDetails.getShiftName());
        shift.setStartTime(shiftDetails.getStartTime());
        shift.setEndTime(shiftDetails.getEndTime());
        shift.setDescription(shiftDetails.getDescription());
        return shiftDAO.save(shift);
    }

    public void deleteShift(Long id) {
        shiftDAO.deleteById(id);
    }
}
