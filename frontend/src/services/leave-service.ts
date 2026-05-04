import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class LeaveService {
   private apiUrl = 'http://localhost:8080/api/leaves';

  constructor(private http: HttpClient) {}

  // Employee - Apply for leave
  applyLeave(leaveData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/apply`, leaveData);
  }

  // Employee - Get my leaves
  getMyLeaves(status?: string): Observable<any[]> {
    let url = `${this.apiUrl}/my-leaves`;
    if (status) {
      url += `?status=${status}`;
    }
    return this.http.get<any[]>(url);
  }

  // Employee - Cancel leave
  cancelLeave(id: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/cancel`, {});
  }

  // Admin - Get all leaves
  getAllLeaves(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/all`);
  }

  // Admin - Get pending leaves
  getPendingLeaves(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/pending`);
  }

  // Admin - Get leaves by status
  getLeavesByStatus(status: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/status/${status}`);
  }

  // Admin - Get employee leaves
  getEmployeeLeaves(employeeId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/employee/${employeeId}`);
  }

  // Admin - Approve leave
  approveLeave(id: string, approverData: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/approve`, approverData);
  }

  // Admin - Reject leave
  rejectLeave(id: string, approverData: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/reject`, approverData);
  }

  // Get statistics
  getLeaveStatistics(): Observable<any> {
    return this.http.get(`${this.apiUrl}/statistics/dashboard`);
  }

  getLeaveById(id: string | null): Observable<any> {
    return this.http.get(`${this.apiUrl}/${id}`);
  }
}
