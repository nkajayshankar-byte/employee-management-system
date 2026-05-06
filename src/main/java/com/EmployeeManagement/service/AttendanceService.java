package com.EmployeeManagement.service;

import com.EmployeeManagement.entity.Attendance;
import com.EmployeeManagement.entity.Shift;
import com.EmployeeManagement.dao.AttendanceDAO;
import com.EmployeeManagement.dao.EmployeeShiftDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceDAO attendanceDAO;

    @Autowired
    private EmployeeShiftDAO employeeShiftDAO;

    public Attendance checkIn(String employeeId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        Optional<Attendance> existing = attendanceDAO.findByEmployeeIdAndDate(employeeId, today);
        if (existing.isPresent()) {
            throw new RuntimeException("Already checked in for today");
        }

        Attendance attendance = new Attendance();
        attendance.setEmployeeId(employeeId);
        attendance.setDate(today);
        
        // Use India Standard Time (IST) as per user's location (+05:30)
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        attendance.setCheckInTime(now);

        // Logic to mark "Late": Status is Present for up to 1 hour after shift start
        // Use a JOIN to get shift details directly
        Shift shift = employeeShiftDAO.findShiftByEmployeeIdAndDate(employeeId, today).orElse(null);
        if (shift != null) {
            LocalTime shiftStartTime = shift.getStartTime();
            LocalTime lateThreshold = shiftStartTime.plusHours(1);
            
            if (now.toLocalTime().isAfter(lateThreshold)) {
                attendance.setStatus("Late");
            } else {
                attendance.setStatus("Present");
            }
        } else {
            attendance.setStatus("Present");
        }

        return attendanceDAO.save(attendance);
    }

    public Attendance checkOut(String employeeId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        Attendance attendance = attendanceDAO.findByEmployeeIdAndDate(employeeId, today)
                .orElseThrow(() -> new RuntimeException("No check-in record found for today"));

        if (attendance.getCheckOutTime() != null) {
            throw new RuntimeException("Already checked out for today");
        }

        attendance.setCheckOutTime(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

        // Calculate working hours
        Duration duration = Duration.between(attendance.getCheckInTime(), attendance.getCheckOutTime());
        double hours = duration.toMinutes() / 60.0;
        attendance.setWorkingHours(hours);

        return attendanceDAO.save(attendance);
    }

    public List<Attendance> getEmployeeAttendance(String employeeId) {
        return attendanceDAO.findByEmployeeId(employeeId);
    }

    public List<Attendance> getAttendanceByDate(LocalDate date) {
        return attendanceDAO.findByDate(date);
    }

    public List<Attendance> getAllAttendance() {
        return attendanceDAO.findAll();
    }
}
