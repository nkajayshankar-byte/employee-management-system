package com.EmployeeManagement.service;

import com.EmployeeManagement.dao.AttendanceDAO;
import com.EmployeeManagement.dao.PayslipDAO;
import com.EmployeeManagement.dao.SalaryStructureDAO;
import com.EmployeeManagement.dao.UserDAO;
import com.EmployeeManagement.dto.PayslipDto;
import com.EmployeeManagement.dto.SalaryStructureDto;
import com.EmployeeManagement.entity.Attendance;
import com.EmployeeManagement.entity.Payslip;
import com.EmployeeManagement.entity.SalaryStructure;
import com.EmployeeManagement.entity.User;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PayrollService {

    @Autowired
    private SalaryStructureDAO salaryStructureDAO;

    @Autowired
    private PayslipDAO payslipDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private AttendanceDAO attendanceDAO;

    // --- Salary Structure Methods ---

    public SalaryStructureDto saveSalaryStructure(SalaryStructureDto dto) {
        SalaryStructure structure = new SalaryStructure();
        structure.setId(dto.getId());
        structure.setEmployeeId(dto.getEmployeeId());
        structure.setBaseSalary(dto.getBaseSalary());
        structure.setHra(dto.getHra());
        structure.setOtherAllowances(dto.getOtherAllowances());
        structure.setTaxDeductions(dto.getTaxDeductions());
        structure.setProvidentFund(dto.getProvidentFund());
        structure.setAccountNumber(dto.getAccountNumber());
        
        SalaryStructure saved = salaryStructureDAO.save(structure);
        return mapToDto(saved);
    }

    public SalaryStructureDto getSalaryStructure(Long employeeId) {
        return salaryStructureDAO.findByEmployeeId(employeeId)
                .map(this::mapToDto)
                .orElse(null);
    }

    // --- Payslip Methods ---

    public PayslipDto generatePayslip(Long employeeId, int month, int year) {
        // Get salary structure
        SalaryStructure structure = salaryStructureDAO.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Salary structure not found for employee ID: " + employeeId));

        User employee = userDAO.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Calculate Paid Days based on Attendance
        YearMonth yearMonthObject = YearMonth.of(year, month);
        int totalDays = yearMonthObject.lengthOfMonth();
        
        List<Attendance> attendances = attendanceDAO.findByEmployeeId(employeeId);
        long presentDays = attendances.stream()
                .filter(a -> a.getDate().getMonthValue() == month && 
                             a.getDate().getYear() == year)
                .filter(a -> "Present".equalsIgnoreCase(a.getStatus()) || "Late".equalsIgnoreCase(a.getStatus()))
                .count();

        if (presentDays > totalDays) {
            presentDays = totalDays; // Sanity check
        }

        long absentDays = totalDays - presentDays;
        int paidDays = (int) presentDays;

        double grossPay = structure.getBaseSalary() + structure.getHra() + structure.getOtherAllowances();
        double dailyGross = grossPay / totalDays;
        
        double lopAmount = dailyGross * absentDays; // Loss of Pay for absent days
        
        // Round to 2 decimal places
        lopAmount = Math.round(lopAmount * 100.0) / 100.0;
        
        double totalDeductions = structure.getTaxDeductions() + structure.getProvidentFund() + lopAmount;
        
        // Cap total deductions so Net Pay is never negative
        if (totalDeductions > grossPay) {
            totalDeductions = grossPay;
        }
        
        double netPay = grossPay - totalDeductions;
        netPay = Math.round(netPay * 100.0) / 100.0; // Round net pay

        // Check if payslip already exists to update it, else create new
        Optional<Payslip> existing = payslipDAO.findByEmployeeIdAndMonthAndYear(employeeId, month, year);
        Payslip payslip = existing.orElse(new Payslip());

        payslip.setEmployeeId(employeeId);
        payslip.setMonth(month);
        payslip.setYear(year);
        payslip.setTotalDays(totalDays);
        payslip.setPaidDays(paidDays);
        payslip.setGrossPay(grossPay);
        payslip.setLopAmount(lopAmount);
        payslip.setTotalDeductions(totalDeductions);
        payslip.setNetPay(netPay);
        payslip.setStatus("GENERATED");

        // Generate PDF
        String pdfUrl = generatePdfFile(payslip, employee);
        payslip.setPdfUrl(pdfUrl);

        Payslip saved = payslipDAO.save(payslip);
        return mapToDto(saved, employee);
    }

    public List<PayslipDto> getEmployeePayslips(Long employeeId) {
        User employee = userDAO.findById(employeeId).orElse(null);
        return payslipDAO.findByEmployeeId(employeeId).stream()
                .map(p -> mapToDto(p, employee))
                .collect(Collectors.toList());
    }

    // --- Helper Methods ---

    private SalaryStructureDto mapToDto(SalaryStructure entity) {
        SalaryStructureDto dto = new SalaryStructureDto();
        dto.setId(entity.getId());
        dto.setEmployeeId(entity.getEmployeeId());
        dto.setBaseSalary(entity.getBaseSalary());
        dto.setHra(entity.getHra());
        dto.setOtherAllowances(entity.getOtherAllowances());
        dto.setTaxDeductions(entity.getTaxDeductions());
        dto.setProvidentFund(entity.getProvidentFund());
        dto.setNetSalary(entity.getNetSalary());
        dto.setAccountNumber(entity.getAccountNumber());
        return dto;
    }

    private PayslipDto mapToDto(Payslip entity, User employee) {
        PayslipDto dto = new PayslipDto();
        dto.setId(entity.getId());
        dto.setEmployeeId(entity.getEmployeeId());
        dto.setMonth(entity.getMonth());
        dto.setYear(entity.getYear());
        dto.setTotalDays(entity.getTotalDays());
        dto.setPaidDays(entity.getPaidDays());
        dto.setGrossPay(entity.getGrossPay());
        dto.setTotalDeductions(entity.getTotalDeductions());
        dto.setLopAmount(entity.getLopAmount());
        dto.setNetPay(entity.getNetPay());
        dto.setStatus(entity.getStatus());
        dto.setPdfUrl(entity.getPdfUrl());
        
        if (employee != null) {
            dto.setEmployeeName(employee.getName());
            dto.setEmployeeEmail(employee.getEmail());
        }
        return dto;
    }

    private String generatePdfFile(Payslip payslip, User employee) {
        String fileName = "Payslip_" + employee.getId() + "_" + payslip.getMonth() + "_" + payslip.getYear() + ".pdf";
        String dirPath = "uploads/payslips/";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath = dirPath + fileName;

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                contentStream.beginText();
                contentStream.setFont(font, 18);
                contentStream.newLineAtOffset(200, 750);
                contentStream.showText("EMPLOYEE PAYSLIP");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(normalFont, 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.setLeading(14.5f);
                
                SalaryStructure salaryStructure = salaryStructureDAO.findByEmployeeId(employee.getId()).orElse(null);
                String acctNo = (salaryStructure != null && salaryStructure.getAccountNumber() != null) ? salaryStructure.getAccountNumber() : "N/A";

                contentStream.showText("Employee Name: " + employee.getName());
                contentStream.newLine();
                contentStream.showText("Email: " + employee.getEmail());
                contentStream.newLine();
                contentStream.showText("Account Number: " + acctNo);
                contentStream.newLine();
                contentStream.showText("Period: " + payslip.getMonth() + "/" + payslip.getYear());
                contentStream.newLine();
                contentStream.showText("Total Days: " + payslip.getTotalDays() + " | Paid Days: " + payslip.getPaidDays());
                contentStream.newLine();
                contentStream.newLine();
                
                contentStream.setFont(font, 12);
                contentStream.showText("Earnings");
                contentStream.newLine();
                contentStream.setFont(normalFont, 12);
                contentStream.showText("Gross Pay: Rs." + payslip.getGrossPay());
                contentStream.newLine();
                contentStream.newLine();
                
                contentStream.setFont(font, 12);
                contentStream.showText("Deductions");
                contentStream.newLine();
                contentStream.setFont(normalFont, 12);
                
                int absentDays = payslip.getTotalDays() - payslip.getPaidDays();
                double lopAmount = payslip.getLopAmount() != null ? payslip.getLopAmount() : 0.0;
                
                double stdDeductions = payslip.getTotalDeductions() - lopAmount;
                // If total deductions were capped, stdDeductions could be negative
                if (stdDeductions < 0) {
                    if (lopAmount > payslip.getTotalDeductions()) {
                        lopAmount = payslip.getTotalDeductions();
                        stdDeductions = 0;
                    } else {
                        stdDeductions = payslip.getTotalDeductions() - lopAmount;
                    }
                }
                
                contentStream.showText("Standard Deductions: Rs." + Math.round(stdDeductions * 100.0) / 100.0);
                contentStream.newLine();
                if (lopAmount > 0) {
                    contentStream.showText("Loss of Pay (" + absentDays + " days): Rs." + lopAmount);
                    contentStream.newLine();
                }
                contentStream.showText("Total Deductions: Rs." + payslip.getTotalDeductions());
                contentStream.newLine();
                contentStream.newLine();
                
                contentStream.setFont(font, 14);
                contentStream.showText("Net Pay: Rs." + payslip.getNetPay());
                
                contentStream.endText();
            }
            document.save(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        String cloudinaryUrl = null;
        try {
            cloudinaryUrl = cloudinaryService.uploadLocalFile(new File(filePath), "image", "payslips/" + fileName.replace(".pdf", ""));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to upload payslip to Cloudinary, falling back to local file.");
        }

        if (cloudinaryUrl != null) {
            return cloudinaryUrl;
        }

        return "/uploads/payslips/" + fileName;
    }
}
