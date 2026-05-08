import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

  export interface Job {
    id?: number | string;
    title: string;
    department: string;
    location: string;
    type: string;
    description: string;
    isActive?: boolean;
  }

  export interface JobApplication {
    id?: number | string;
    jobId: number | string;
    jobTitle?: string;
    jobActive?: boolean;
    employeeId: number | string;
    employeeName: string;
    employeeEmail: string;
    status?: string;
    appliedDate?: Date;
  }
@Injectable({
  providedIn: 'root',
})
 export class CareerService {
    private apiUrl = `${environment.apiUrl}/api/careers`;

    constructor(private http: HttpClient) {}

    getJobs(): Observable<Job[]> {
      return this.http.get<Job[]>(`${this.apiUrl}/jobs`);
    }

    saveJob(job: Job): Observable<Job> {
      if (job.id) {
        return this.http.put<Job>(`${this.apiUrl}/jobs/${job.id}`, job);
      } else {
        return this.http.post<Job>(`${this.apiUrl}/jobs`, job);
      }
    }

    updateJob(id: number | string, job: Job): Observable<Job> {
      return this.http.put<Job>(`${this.apiUrl}/jobs/${id}`, job);
    }

    deleteJob(id: number | string): Observable<any> {
      return this.http.delete(`${this.apiUrl}/jobs/${id}`);
    }

    apply(application: JobApplication): Observable<JobApplication> {
      return this.http.post<JobApplication>(`${this.apiUrl}/apply`, application);
    }

    getApplicants(jobId: number | string): Observable<JobApplication[]> {
      return this.http.get<JobApplication[]>(`${this.apiUrl}/applications/${jobId}`);
    }

    hasApplied(jobId: number | string, employeeId: number | string): Observable<boolean> {
     return this.http.get<boolean>(`${this.apiUrl}/check-application?jobId=${jobId}&empId=${employeeId}`);
    }

    updateStatus(id: number | string, status: string): Observable<any> {
      return this.http.put(`${this.apiUrl}/applications/status/${id}`, { status });
    }

    getMyApplications(employeeId: number | string): Observable<JobApplication[]> {
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
