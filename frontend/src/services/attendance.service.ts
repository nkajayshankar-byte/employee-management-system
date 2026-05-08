import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface Attendance {
  id?: number | string;
  employeeId: number | string;
  date: string;
  checkInTime: string;
  checkOutTime?: string;
  status: string; 
  workingHours?: number;
}

@Injectable({
  providedIn: 'root'
})
export class AttendanceService {
  private apiUrl = `${environment.apiUrl}/api/attendance`;

  constructor(private http: HttpClient) {}

  checkIn(employeeId: number | string): Observable<Attendance> {
    const params = new HttpParams().set('employeeId', employeeId.toString());
    return this.http.post<Attendance>(`${this.apiUrl}/check-in`, {}, { params });
  }

  checkOut(employeeId: number | string): Observable<Attendance> {
    const params = new HttpParams().set('employeeId', employeeId.toString());
    return this.http.post<Attendance>(`${this.apiUrl}/check-out`, {}, { params });
  }

  getEmployeeAttendance(employeeId: number | string): Observable<Attendance[]> {
    return this.http.get<Attendance[]>(`${this.apiUrl}/employee/${employeeId}`);
  }

  getAttendanceByDate(date: string): Observable<Attendance[]> {
    return this.http.get<Attendance[]>(`${this.apiUrl}/date/${date}`);
  }
}
