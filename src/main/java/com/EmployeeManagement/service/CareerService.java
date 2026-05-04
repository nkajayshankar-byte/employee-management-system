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

    public List<JobDTO> getAllJobs() { 
        List<Job> jobs = jobDAO.findAll();
        if (jobs.isEmpty()) {
            Job j1 = new Job(null, "Senior Java Developer", "Engineering", "Bangalore", "Full-time", "Work on high-scale systems.", 1200000, 2500000, Arrays.asList("Java", "Spring Boot", "MySQL"),LocalDateTime.now(ZoneId.of("Asia/Kolkata")), "NA");
            Job j2 = new Job(null, "Product Designer", "Design", "Remote", "Full-time", "Create beautiful user experiences.", 800000, 1800000,Arrays.asList("Figma", "UI/UX", "Adobe Suite"),LocalDateTime.now(ZoneId.of("Asia/Kolkata")),"NA");
            Job j3 = new Job(null, "QA Engineer", "Engineering", "Mumbai", "Contract", "Ensure top-notch quality.", 600000, 1200000, Arrays.asList("Selenium", "JUnit", "Testing"), LocalDateTime.now(ZoneId.of("Asia/Kolkata")),"NA");
            jobDAO.save(j1);
            jobDAO.save(j2);
            jobDAO.save(j3);
            jobs = jobDAO.findAll();
        }
        return jobs.stream().map(jobMapper::toDTO).collect(Collectors.toList()); 
    }

    public void addJob(JobDTO jobDto) { 
        jobDAO.save(jobMapper.toEntity(jobDto)); 
    }

    public void updateJob(JobDTO jobDto) { 
        jobDAO.save(jobMapper.toEntity(jobDto)); 
    }
    
    public void deleteJob(String id) { 
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
        
        if (dto.getJobId() == null || dto.getJobId().isEmpty()) {
            throw new RuntimeException("Job ID is missing in the application.");
        }
        
        Optional<Job> jobCheck = jobDAO.findById(dto.getJobId());
        if (jobCheck.isEmpty() || !jobCheck.get().getIsActive()) {
            throw new RuntimeException("This position is no longer accepting applications.");
        }
        
        if (dto.getEmployeeId() == null || dto.getEmployeeId().isEmpty()) {
            throw new RuntimeException("Employee ID (User ID) is missing in the application. Please re-login.");
        }

        boolean alreadyApplied = appDAO.existsByJobIdAndEmployeeId(dto.getJobId(), dto.getEmployeeId());

        if (alreadyApplied) {
            throw new RuntimeException("You have already applied for this position.");
        }

        Optional<User> userOpt = userDAO.findById(dto.getEmployeeId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (dto.getEmployeeName() == null || dto.getEmployeeName().isEmpty()) {
                dto.setEmployeeName(user.getName());
            }
            if (dto.getEmployeeEmail() == null || dto.getEmployeeEmail().isEmpty()) {
                dto.setEmployeeEmail(user.getEmail());
            }
        }

        if (dto.getStatus() == null) {
            dto.setStatus("PENDING");
        }
        
        dto.setAppliedDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        JobApplication application = applicationMapper.toEntity(dto);
        JobApplication savedApp = appDAO.save(application);

        try {
            String jobTitle = jobCheck.get().getTitle();
            if (dto.getEmployeeEmail() != null && !dto.getEmployeeEmail().isEmpty()) {
                emailService.sendApplicationReceivedEmail(dto.getEmployeeEmail(), 
                    dto.getEmployeeName() != null ? dto.getEmployeeName() : "Applicant", 
                    jobTitle);
            }
        } catch (Exception e) {
            System.err.println("Failed to send application confirmation email: " + e.getMessage());
        }
        
        return applicationMapper.toDTO(savedApp);
    }

    public boolean hasApplied(String jobId, String employeeId) {
        if (jobId == null || employeeId == null) return false;
        return appDAO.existsByJobIdAndEmployeeId(jobId, employeeId);
    }

    public List<JobApplicationDTO> getApplicationsByJob(String jobId) {
        return appDAO.findByJobId(jobId).stream()
            .map(applicationMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    public List<JobApplicationDTO> getApplicationsByEmployee(String empId) {
        return appDAO.findByEmployeeId(empId).stream()
            .map(applicationMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    public void updateApplicationStatus(String applicationId, String newStatus) {
        Optional<JobApplication> appOpt = appDAO.findById(applicationId);
        if (appOpt.isPresent()) {
            JobApplication app = appOpt.get();

            List<String> validStatuses = Arrays.asList("SHORTLISTED", "REJECTED", "PENDING", "HIRED");
            if (!validStatuses.contains(newStatus)) {
                throw new RuntimeException("Invalid status value provided.");
            }

            app.setStatus(newStatus);
            app.setAppliedDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            appDAO.save(app);

            Optional<Job> jobOpt = jobDAO.findById(app.getJobId());
            String jobTitle = jobOpt.isPresent() ? jobOpt.get().getTitle() : "Unknown Position";

            if ("HIRED".equals(newStatus)) {
                Optional<User> userOpt = userDAO.findById(app.getEmployeeId());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    if (user.getRole() == Role.USER) {
                        user.setRole(Role.EMPLOYEE);
                        user.setJobRole(jobTitle); // Set the hired job title
                        userDAO.save(user);
                    }
                }
            }
            
            if (app.getEmployeeEmail() != null && !"Unknown".equals(app.getEmployeeEmail())) {
                emailService.sendStatusUpdateEmail(app.getEmployeeEmail(), app.getEmployeeName(), jobTitle, newStatus);
            }
        }
    }

    public List<JobDTO> searchJobs(String query, Integer minSalary, String location) {
        return jobDAO.findAll().stream()
            .filter(Job::getIsActive)
            .filter(job -> {
                if (query == null || query.trim().isEmpty()) return true;
                String lowerQuery = query.toLowerCase();
                return job.getTitle().toLowerCase().contains(lowerQuery) || 
                       job.getDescription().toLowerCase().contains(lowerQuery) ||
                       job.getLocation().toLowerCase().contains(lowerQuery) ||
                       job.getDepartment().toLowerCase().contains(lowerQuery);
            })
            .filter(job -> (minSalary == null || job.getMaxSalary() >= minSalary))
            .map(jobMapper::toDTO)
            .collect(Collectors.toList());
    }
}