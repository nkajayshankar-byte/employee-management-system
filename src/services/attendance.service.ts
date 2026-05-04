import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Attendance {
  id?: string;
  employeeId: string;
  date: string;
  checkInTime: string;
  checkOutTime?: string;
  status: string; // Present, Absent, Late
  workingHours?: number;
}

@Injectable({
  providedIn: 'root'
})
export class AttendanceService {
  private apiUrl = 'http://localhost:8080/api/attendance';

  constructor(private http: HttpClient) {}

  checkIn(employeeId: string): Observable<Attendance> {
    const params = new HttpParams().set('employeeId', employeeId);
    return this.http.post<Attendance>(`${this.apiUrl}/check-in`, {}, { params });
  }

  checkOut(employeeId: string): Observable<Attendance> {
    const params = new HttpParams().set('employeeId', employeeId);
    return this.http.post<Attendance>(`${this.apiUrl}/check-out`, {}, { params });
  }

  getEmployeeAttendance(employeeId: string): Observable<Attendance[]> {
    return this.http.get<Attendance[]>(`${this.apiUrl}/employee/${employeeId}`);
  }

  getAttendanceByDate(date: string): Observable<Attendance[]> {
    return this.http.get<Attendance[]>(`${this.apiUrl}/date/${date}`);
  }


}
