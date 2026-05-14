package com.EmployeeManagement.controller;

import com.EmployeeManagement.dto.JobApplicationDTO;
import com.EmployeeManagement.dto.JobDTO;
import com.EmployeeManagement.service.CareerService;
import com.EmployeeManagement.service.FileStorageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/careers")
public class CareerController {

    @Autowired
    private CareerService careerService;

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping(value = "/upload-resume", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> uploadResume(@RequestPart("file") MultipartFile file) {
        String url = fileStorageService.save(file);
        Map<String, String> res = new HashMap<>();
        res.put("url", url);
        return res;
    }

    @GetMapping("/jobs")
    public List<JobDTO> getAllJobs() {
        return careerService.getAllJobs();
    }

    @PostMapping("/jobs")
    public JobDTO addJob(@RequestBody JobDTO jobDto) {
        careerService.addJob(jobDto);
        return jobDto;
    }

    @PutMapping("/jobs/{id}")
    public JobDTO updateJob(@PathVariable("id") Long id, @RequestBody JobDTO jobDto) {
        jobDto.setId(id);
        careerService.updateJob(jobDto);
        return jobDto;
    }

    @DeleteMapping("/jobs/{id}")
    public Map<String, String> deleteJob(@PathVariable("id") Long id) {
        careerService.deleteJob(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Job deleted successfully");
        return response;
    }

    @PostMapping("/apply")
    public JobApplicationDTO apply(@RequestBody JobApplicationDTO dto) {
    	return careerService.applyForJob(dto);
    }

    @GetMapping("/applications/{jobId}")
    public List<JobApplicationDTO> getApplicants(@PathVariable("jobId") String jobId) {
        try {
            Long id = Long.parseLong(jobId.trim());
            return careerService.getApplicationsByJob(id);
        } catch (NumberFormatException e) {
            throw e;
        }
    }
    
    @GetMapping("/applications/employee/{empId}")
    public List<JobApplicationDTO> getApplicationsByEmployee(@PathVariable("empId") Long empId) {
        return careerService.getApplicationsByEmployee(empId);
    }
    
    @GetMapping("/check-application")
    public boolean checkApplication(
            @RequestParam("jobId") Long jobId,
            @RequestParam("empId") Long empId) {
        return careerService.hasApplied(jobId, empId);
    }

    @PutMapping("/applications/status/{id}")
    public void updateStatus(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        careerService.updateApplicationStatus(id, status);
    }

    @GetMapping("/search")
    public List<JobDTO> searchJobs(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "minSalary", required = false) Integer minSalary,
            @RequestParam(value = "location", required = false) String location) {
        return careerService.searchJobs(query, minSalary, location);
    }
}