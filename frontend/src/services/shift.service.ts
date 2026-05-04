import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Shift {
  id?: string;
  shiftName: string;
  startTime: string;
  endTime: string;
  description: string;
}

export interface EmployeeShift {
  id?: string;
  employeeId: string;
  shiftId: string;
  startDate: string;
  endDate: string;
}

@Injectable({
  providedIn: 'root'
})
export class ShiftService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // Shift Management
  getShifts(): Observable<Shift[]> {
    return this.http.get<Shift[]>(`${this.apiUrl}/shifts`);
  }

  createShift(shift: Shift): Observable<Shift> {
    return this.http.post<Shift>(`${this.apiUrl}/shifts`, shift);
  }

  updateShift(id: string, shift: Shift): Observable<Shift> {
    return this.http.put<Shift>(`${this.apiUrl}/shifts/${id}`, shift);
  }

  deleteShift(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/shifts/${id}`);
  }

  // Shift Assignment
  assignShift(assignment: EmployeeShift): Observable<EmployeeShift> {
    return this.http.post<EmployeeShift>(`${this.apiUrl}/shift-assign`, assignment);
  }

  bulkAssign(bulkData: any): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/shift-assign/bulk`, bulkData);
  }

  getEmployeeShifts(employeeId: string): Observable<EmployeeShift[]> {
    return this.http.get<EmployeeShift[]>(`${this.apiUrl}/shift-assign/employee/${employeeId}`);
  }

  getAllAssignments(): Observable<EmployeeShift[]> {
    return this.http.get<EmployeeShift[]>(`${this.apiUrl}/shift-assign/all`);
  }

  getAssignmentsByDate(date: string): Observable<EmployeeShift[]> {
    return this.http.get<EmployeeShift[]>(`${this.apiUrl}/shift-assign/date/${date}`);
  }

  deleteAssignment(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/shift-assign/${id}`);
  }

  bulkDeleteAssignments(ids: string[]): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/shift-assign/bulk-delete`, ids);
  }
}
