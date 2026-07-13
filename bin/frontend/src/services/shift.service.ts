import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface Shift {
  id?: number | string;
  shiftName: string;
  startTime: string;
  endTime: string;
  description: string;
}

export interface EmployeeShift {
  id?: number | string;
  employeeId: number | string;
  shiftId: number | string;
  startDate: string;
  endDate: string;
}

@Injectable({
  providedIn: 'root'
})
export class ShiftService {
    private apiUrl = `${environment.apiUrl}/api`;

  constructor(private http: HttpClient) {}

  // Shift Management
  getShifts(): Observable<Shift[]> {
    return this.http.get<Shift[]>(`${this.apiUrl}/shifts`);
  }

  createShift(shift: Shift): Observable<Shift> {
    return this.http.post<Shift>(`${this.apiUrl}/shifts`, shift);
  }

  updateShift(id: number | string, shift: Shift): Observable<Shift> {
    return this.http.put<Shift>(`${this.apiUrl}/shifts/${id}`, shift);
  }

  deleteShift(id: number | string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/shifts/${id}`);
  }

  // Shift Assignment
  assignShift(assignment: EmployeeShift): Observable<EmployeeShift> {
    return this.http.post<EmployeeShift>(`${this.apiUrl}/shift-assign`, assignment);
  }

  bulkAssign(bulkData: any): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/shift-assign/bulk`, bulkData);
  }

  getEmployeeShifts(employeeId: number | string): Observable<EmployeeShift[]> {
    return this.http.get<EmployeeShift[]>(`${this.apiUrl}/shift-assign/employee/${employeeId}`);
  }

  getAllAssignments(): Observable<EmployeeShift[]> {
    return this.http.get<EmployeeShift[]>(`${this.apiUrl}/shift-assign/all`);
  }

  getAssignmentsByDate(date: string): Observable<EmployeeShift[]> {
    return this.http.get<EmployeeShift[]>(`${this.apiUrl}/shift-assign/date/${date}`);
  }

  deleteAssignment(id: number | string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/shift-assign/${id}`);
  }

  bulkDeleteAssignments(ids: (number | string)[]): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/shift-assign/bulk-delete`, ids);
  }
}
