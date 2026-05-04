import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

 export interface Job {
    id?: string;
    title: string;
    department: string;
    location: string;
    type: string;
    description: string;
  }

  export interface JobApplication {
    id?: string;
    jobId: string;
    employeeId: string;
    employeeName: string;
    employeeEmail: string;
    appliedDate?: Date;
  }
@Injectable({
  providedIn: 'root',
})
 export class CareerService {
    // Replace with your actual backend API URL
    private apiUrl = 'http://localhost:8080/api/careers';

    constructor(private http: HttpClient) {}

    // --- Job Management (Admin) ---

    /**
     * Fetch all available job postings
     */
    getJobs(): Observable<Job[]> {
      return this.http.get<Job[]>(`${this.apiUrl}/jobs`);
    }

    /**
     * Add or Update a job posting
     */
    saveJob(job: Job): Observable<Job> {
      if (job.id) {
        // Update existing job
        return this.http.put<Job>(`${this.apiUrl}/jobs/${job.id}`, job);
      } else {
        // Create new job
        return this.http.post<Job>(`${this.apiUrl}/jobs`, job);
      }
    }

    updateJob(id: string, job: Job): Observable<Job> {
      return this.http.put<Job>(`${this.apiUrl}/jobs/${id}`, job);
    }

    /**
     * Delete a job posting
     */
    deleteJob(id: string): Observable<any> {
      return this.http.delete(`${this.apiUrl}/jobs/${id}`);
    }

    // --- Application Management ---

    /**
     * Submit a job application (Employee)
     */
    apply(application: JobApplication): Observable<JobApplication> {
      return this.http.post<JobApplication>(`${this.apiUrl}/apply`, application);
    }

    /**
     * Get all employees who applied for a specific job (Admin)
     */
    getApplicants(jobId: string): Observable<JobApplication[]> {
      return this.http.get<JobApplication[]>(`${this.apiUrl}/applications/${jobId}`);
    }

    hasApplied(jobId: string, employeeId: string): Observable<boolean> {
     return this.http.get<boolean>(`${this.apiUrl}/check-application?jobId=${jobId}&empId=${employeeId}`);
    }

    updateStatus(id: string, status: string): Observable<any> {
      return this.http.put(`${this.apiUrl}/applications/status/${id}`, { status });
    }
    getMyApplications(employeeId: string): Observable<JobApplication[]> {
      return this.http.get<JobApplication[]>(
        `${this.apiUrl}/applications/employee/${employeeId}`
      );
    }

    searchJobs(query: string, minSalary: number, location: string): Observable<Job[]> {
      let params = `?query=${query}&minSalary=${minSalary}&location=${location}`;
      return this.http.get<Job[]>(`${this.apiUrl}/search${params}`);
    }

    uploadResume(file: File): Observable<any> {
      const formData = new FormData();
      formData.append('file', file);
      return this.http.post(`${this.apiUrl}/upload-resume`, formData);
    }
  }
