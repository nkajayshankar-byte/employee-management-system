package com.EmployeeManagement.service;

import com.EmployeeManagement.dto.LeaveDTO;
import com.EmployeeManagement.entity.Leave;
import com.EmployeeManagement.entity.User;
import com.EmployeeManagement.mapper.LeaveMapper;
import com.EmployeeManagement.dao.LeaveDAO;
import com.EmployeeManagement.dao.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class LeaveService {

    @Autowired
    private LeaveDAO leaveDAO;
    
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private LeaveMapper leaveMapper;

    @Autowired
    private EmailService emailService;

    // ✅ Apply Leave
    public String applyLeave(LeaveDTO leaveDto, String employeeId) {

        // 1. Fetch user from DB
        Optional<User> userOpt = userDAO.findById(employeeId);

        if (userOpt.isEmpty()) {
            return "User not found";
        }

        User user = userOpt.get();
        Leave leave = leaveMapper.toEntity(leaveDto);

        // 2. SET employee details
        leave.setEmployeeId(employeeId);
        leave.setEmployeeName(user.getName());

        // 3. Business logic
        if (leave.getStartDate().isAfter(leave.getEndDate())) {
            return "Start date must be before end date";
        }

        int workingDays = calculateWorkingDays(leave.getStartDate(), leave.getEndDate());

        leave.setNumberOfDays(workingDays);
        leave.setStatus("PENDING");
        leave.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        leave.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

        // 4. Save
        leaveDAO.save(leave);

        return "Leave applied successfully";
    }

    // ✅ Get My Leaves
    public List<LeaveDTO> getMyLeaves(String employeeId, String status) {
        List<Leave> leaves;
        if (status != null && !status.isEmpty()) {
            leaves = leaveDAO.findByEmployeeIdAndStatus(employeeId, status);
        } else {
            leaves = leaveDAO.findByEmployeeId(employeeId);
        }
        
        List<LeaveDTO> dtoList = new ArrayList<>();
        for (Leave leave : leaves) {
            dtoList.add(leaveMapper.toDTO(leave));
        }
        return dtoList;
    }

    // ✅ Cancel Leave
    public String cancelLeave(String id) {

        Optional<Leave> leaveOpt = leaveDAO.findById(id);

        if (leaveOpt.isEmpty()) {
            return "NOT_FOUND";
        }

        Leave leave = leaveOpt.get();

        if (!"PENDING".equals(leave.getStatus())) {
            return "Only pending leaves can be cancelled";
        }

        leave.setStatus("CANCELLED");
        leave.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        leaveDAO.save(leave);

        return "Leave cancelled successfully";
    }

    // ✅ Pending Leaves
    public List<LeaveDTO> getPendingLeaves() {
        List<Leave> leaves = leaveDAO.findPendingLeaves();
        List<LeaveDTO> dtoList = new ArrayList<>();
        for (Leave leave : leaves) {
            dtoList.add(leaveMapper.toDTO(leave));
        }
        return dtoList;
    }

    // ✅ All Leaves
    public List<LeaveDTO> getAllLeaves() {
        List<Leave> leaves = leaveDAO.findAll();
        List<LeaveDTO> dtoList = new ArrayList<>();
        for (Leave leave : leaves) {
            dtoList.add(leaveMapper.toDTO(leave));
        }
        return dtoList;
    }

    // ✅ Leaves by Status
    public List<LeaveDTO> getLeavesByStatus(String status) {
        List<Leave> leaves = leaveDAO.findByStatus(status);
        List<LeaveDTO> dtoList = new ArrayList<>();
        for (Leave leave : leaves) {
            dtoList.add(leaveMapper.toDTO(leave));
        }
        return dtoList;
    }

    // ✅ Employee Leaves (Admin)
    public List<LeaveDTO> getEmployeeLeaves(String employeeId) {
        List<Leave> leaves = leaveDAO.findByEmployeeId(employeeId);
        List<LeaveDTO> dtoList = new ArrayList<>();
        for (Leave leave : leaves) {
            dtoList.add(leaveMapper.toDTO(leave));
        }
        return dtoList;
    }

    // ✅ Approve Leave
    public String approveLeave(String id, String approverId, String approverName, String comments) {

        Optional<Leave> leaveOpt = leaveDAO.findById(id);

        if (leaveOpt.isEmpty()) {
            return "NOT_FOUND";
        }

        Leave leave = leaveOpt.get();

        leave.setStatus("APPROVED");
        leave.setApproverId(approverId);
        leave.setApproverName(approverName);
        leave.setApprovedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        leave.setApproverComments(comments);
        leave.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

        leaveDAO.save(leave);

        Optional<User> empOpt = userDAO.findById(leave.getEmployeeId());
        String empEmail = empOpt.isPresent() ? empOpt.get().getEmail() : null;

        Optional<User> adminOpt = userDAO.findById(approverId);
        String adminEmail = adminOpt.isPresent() ? adminOpt.get().getEmail() : "admin@company.com";

        if (empEmail != null) {
            emailService.sendLeaveStatusEmail(empEmail, leave.getEmployeeName(), "APPROVED", adminEmail, approverName);
        }

        return "Leave approved successfully";
    }

    // ✅ Reject Leave
    public String rejectLeave(String id, String approverId, String approverName, String comments) {

        Optional<Leave> leaveOpt = leaveDAO.findById(id);

        if (leaveOpt.isEmpty()) {
            return "NOT_FOUND";
        }

        Leave leave = leaveOpt.get();

        leave.setStatus("REJECTED");
        leave.setApproverId(approverId);
        leave.setApproverName(approverName);
        leave.setApprovedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        leave.setApproverComments(comments);
        leave.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

        leaveDAO.save(leave);

        Optional<User> empOpt = userDAO.findById(leave.getEmployeeId());
        String empEmail = empOpt.isPresent() ? empOpt.get().getEmail() : null;

        Optional<User> adminOpt = userDAO.findById(approverId);
        String adminEmail = adminOpt.isPresent() ? adminOpt.get().getEmail() : "admin@company.com";

        if (empEmail != null) {
            emailService.sendLeaveStatusEmail(empEmail, leave.getEmployeeName(), "REJECTED", adminEmail, approverName);
        }

        return "Leave rejected successfully";
    }

    // ✅ Statistics
    public Map<String, Object> getStatistics() {

        List<Leave> allLeaves = leaveDAO.findAll();

        int pending = 0;
        int approved = 0;
        int rejected = 0;
        int cancelled = 0;

        for (Leave l : allLeaves) {
            if ("PENDING".equals(l.getStatus())) {
                pending++;
            } else if ("APPROVED".equals(l.getStatus())) {
                approved++;
            } else if ("REJECTED".equals(l.getStatus())) {
                rejected++;
            } else if ("CANCELLED".equals(l.getStatus())) {
                cancelled++;
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalLeaves", allLeaves.size());
        stats.put("pendingLeaves", pending);
        stats.put("approvedLeaves", approved);
        stats.put("rejectedLeaves", rejected);
        stats.put("cancelledLeaves", cancelled);

        return stats;
    }
    
    public LeaveDTO getLeaveById(String id) {
        return leaveDAO.findById(id)
                .map(leaveMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Leave not found"));
    }

    private int calculateWorkingDays(LocalDate startDate, LocalDate endDate) {
        int workingDays = 0;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (current.getDayOfWeek() != DayOfWeek.SATURDAY && current.getDayOfWeek() != DayOfWeek.SUNDAY) {
                workingDays++;
            }
            current = current.plusDays(1);
        }
        return workingDays;
    }
}
