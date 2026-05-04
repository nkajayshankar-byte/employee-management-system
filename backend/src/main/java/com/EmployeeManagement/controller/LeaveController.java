package com.EmployeeManagement.controller;

import com.EmployeeManagement.dto.ApiResponse;
import com.EmployeeManagement.dto.ApprovalRequest;
import com.EmployeeManagement.dto.LeaveDTO;
import com.EmployeeManagement.service.JwtService;
import com.EmployeeManagement.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.*;

@RestController
@RequestMapping("/api/leaves")
@CrossOrigin(origins = "http://localhost:4200")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;
    
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private JwtService jwtService;

    private String getEmployeeIdFromToken() {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtService.extractUserId(token);
        }

        throw new RuntimeException("Invalid or missing token");
    }

    @PostMapping("/apply")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse> applyLeave(@RequestBody LeaveDTO leaveDto) {
        String result = leaveService.applyLeave(leaveDto, getEmployeeIdFromToken());
        if (result.contains("Start date")) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, result));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, result));
    }

    @GetMapping("/my-leaves")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<LeaveDTO>> getMyLeaves(@RequestParam(required = false) String status) {
        List<LeaveDTO> leaves = leaveService.getMyLeaves(getEmployeeIdFromToken(), status);
        return ResponseEntity.ok(leaves);
    }
    
    @GetMapping("/{id}")
    public LeaveDTO getLeaveById(@PathVariable String id) {
        return leaveService.getLeaveById(id);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse> cancelLeave(@PathVariable String id) {
        String result = leaveService.cancelLeave(id);
        if (result.equals("NOT_FOUND")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Leave not found"));
        }
        if (result.contains("Only pending")) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, result));
        }
        return ResponseEntity.ok(new ApiResponse(true, result));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaveDTO>> getPendingLeaves() {
        return ResponseEntity.ok(leaveService.getPendingLeaves());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaveDTO>> getAllLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaveDTO>> getLeavesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(leaveService.getLeavesByStatus(status));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaveDTO>> getEmployeeLeaves(@PathVariable String employeeId) {
        return ResponseEntity.ok(leaveService.getEmployeeLeaves(employeeId));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> approveLeave(@PathVariable String id,
                                                    @RequestBody ApprovalRequest request) {
        String result = leaveService.approveLeave(
                id,
                getEmployeeIdFromToken(),
                request.getApproverName(),
                request.getComments()
        );
        if (result.equals("NOT_FOUND")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Leave not found"));
        }
        return ResponseEntity.ok(new ApiResponse(true, result));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> rejectLeave(@PathVariable String id,
                                                   @RequestBody ApprovalRequest request) {
        String result = leaveService.rejectLeave(
                id,
                getEmployeeIdFromToken(),
                request.getApproverName(),
                request.getComments()
        );
        if (result.equals("NOT_FOUND")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Leave not found"));
        }
        return ResponseEntity.ok(new ApiResponse(true, result));
    }

    @GetMapping("/statistics/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(leaveService.getStatistics());
    }
}