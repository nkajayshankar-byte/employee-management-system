package com.EmployeeManagement.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.EmployeeManagement.dto.JobApplicationDTO;
import com.EmployeeManagement.dto.JobDTO;
import com.EmployeeManagement.entity.Job;
import com.EmployeeManagement.entity.JobApplication;
import com.EmployeeManagement.entity.Role;
import com.EmployeeManagement.entity.User;
import com.EmployeeManagement.mapper.ApplicationMapper;
import com.EmployeeManagement.mapper.JobMapper;
import com.EmployeeManagement.dao.ApplicationDAO;
import com.EmployeeManagement.dao.JobDAO;
import com.EmployeeManagement.dao.UserDAO;

@Service
public class CareerService {
    @Autowired
    private JobDAO jobDAO;

    @Autowired
    private ApplicationDAO appDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JobMapper jobMapper;

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private ResumeScreeningService screeningService;

    public List<JobDTO> getAllJobs() { 
        List<Job> jobs = jobDAO.findAll();
        // Return only active jobs
        return jobs.stream()
            .filter(Job::getIsActive)
            .map(jobMapper::toDTO)
            .collect(Collectors.toList()); 
    }

    public void addJob(JobDTO jobDto) { 
        jobDAO.save(jobMapper.toEntity(jobDto)); 
    }

    public void updateJob(JobDTO jobDto) { 
        jobDAO.save(jobMapper.toEntity(jobDto)); 
    }
    
    public void deleteJob(Long id) { 
        Optional<Job> jobOpt = jobDAO.findById(id);
        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();
            job.setIsActive(false);
            jobDAO.save(job);
        }
    }

    public JobApplicationDTO applyForJob(JobApplicationDTO dto) {
        if (dto == null) {
            throw new RuntimeException("Application data is missing (null body).");
        }
        
        if (dto.getJobId() == null) {
            throw new RuntimeException("Job ID is missing in the application.");
        }
        
        Optional<Job> jobCheck = jobDAO.findById(dto.getJobId());
        if (jobCheck.isEmpty() || !jobCheck.get().getIsActive()) {
            throw new RuntimeException("This position is no longer accepting applications.");
        }
        
        if (dto.getEmployeeId() == null) {
            throw new RuntimeException("Employee ID is missing in the application.");
        }

        boolean alreadyApplied = appDAO.existsByJobIdAndEmployeeId(dto.getJobId(), dto.getEmployeeId());
        if (alreadyApplied) {
            throw new RuntimeException("You have already applied for this position.");
        }

        dto.setStatus("PENDING");
        dto.setAppliedDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        
        JobApplication application = applicationMapper.toEntity(dto);
        JobApplication savedApp = appDAO.save(application);


        try {
            String jobTitle = jobCheck.get().getTitle();
            if (savedApp.getEmployeeEmail() != null) {
                emailService.sendApplicationReceivedEmail(savedApp.getEmployeeEmail(), 
                    savedApp.getEmployeeName() != null ? savedApp.getEmployeeName() : "Applicant", 
                    jobTitle);
            }
        } catch (Exception e) {
            System.err.println("Failed to send application confirmation email: " + e.getMessage());
        }
        
        // Trigger AI Screening
        try {
            if (savedApp.getResumeUrl() != null && !savedApp.getResumeUrl().isEmpty()) {
                var aiAnalysis = screeningService.screenResume(savedApp.getResumeUrl(), savedApp.getJobId());
                savedApp.setMatchPercentage(aiAnalysis.getMatchPercentage());
                savedApp.setMissingSkills(String.join(", ", aiAnalysis.getMissingSkills()));
                savedApp.setStrengths(String.join(", ", aiAnalysis.getStrengths()));
                savedApp.setSummary(aiAnalysis.getSummary());
                savedApp.setExtractedSkills(aiAnalysis.getExtractedSkills());
                savedApp.setExtractedExperience(aiAnalysis.getExtractedExperience());
                savedApp.setExtractedEducation(aiAnalysis.getExtractedEducation());
                
                // Save AI insights
                appDAO.save(savedApp);
            }
        } catch (Exception e) {
            System.err.println("AI Screening failed: " + e.getMessage());
        }

        // Fetch again to get joined fields (name, email, title) and AI insights
        JobApplication fullApp = appDAO.findById(savedApp.getId()).orElse(savedApp);
        
        return applicationMapper.toDTO(fullApp);
    }

    public boolean hasApplied(Long jobId, Long employeeId) {
        if (jobId == null || employeeId == null) return false;
        return appDAO.existsByJobIdAndEmployeeId(jobId, employeeId);
    }

    public List<JobApplicationDTO> getApplicationsByJob(Long jobId) {
        return appDAO.findByJobId(jobId).stream()
            .map(applicationMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    public List<JobApplicationDTO> getApplicationsByEmployee(Long empId) {
        return appDAO.findByEmployeeId(empId).stream()
            .map(applicationMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    public void updateApplicationStatus(Long applicationId, String newStatus) {
        Optional<JobApplication> appOpt = appDAO.findById(applicationId);
        if (appOpt.isPresent()) {
            JobApplication app = appOpt.get();

            List<String> validStatuses = Arrays.asList("SHORTLISTED", "REJECTED", "PENDING", "HIRED");
            if (!validStatuses.contains(newStatus)) {
                throw new RuntimeException("Invalid status value provided.");
            }

            app.setStatus(newStatus);
            appDAO.save(app);

            Optional<Job> jobOpt = jobDAO.findById(app.getJobId());
            String jobTitle = jobOpt.map(Job::getTitle).orElse("Unknown Position");

            if ("HIRED".equals(newStatus)) {
                Optional<User> userOpt = userDAO.findById(app.getEmployeeId());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    if (user.getRole() == Role.USER) {
                        user.setRole(Role.EMPLOYEE);
                        user.setJobRole(jobTitle); 
                        userDAO.save(user);
                    }
                }
            }
            
            // Re-fetch to ensure we have name and email for the email notification
            JobApplication fullApp = appDAO.findById(applicationId).get();
            if (fullApp.getEmployeeEmail() != null) {
                emailService.sendStatusUpdateEmail(fullApp.getEmployeeEmail(), fullApp.getEmployeeName(), jobTitle, newStatus);
            }
        }
    }

    public List<JobDTO> searchJobs(String query, Integer minSalary, String location) {
        // Optimization: In a real app, this would be a DAO method with SQL WHERE clauses.
        // For now, we filter in memory to keep the DAO simple.
        return jobDAO.findAll().stream()
            .filter(Job::getIsActive)
            .filter(job -> {
                if (query == null || query.trim().isEmpty()) return true;
                String lowerQuery = query.toLowerCase();
                return job.getTitle().toLowerCase().contains(lowerQuery) || 
                       job.getDescription().toLowerCase().contains(lowerQuery) ||
                       (job.getLocation() != null && job.getLocation().toLowerCase().contains(lowerQuery)) ||
                       (job.getDepartment() != null && job.getDepartment().toLowerCase().contains(lowerQuery));
            })
            .filter(job -> (minSalary == null || job.getMaxSalary() >= minSalary))
            .map(jobMapper::toDTO)
            .collect(Collectors.toList());
    }
}