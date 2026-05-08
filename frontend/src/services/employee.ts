import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface Employee {
  id?: number | string;
  email: string;
  name: string;
  mobile?: string;
  address?: string;
  role: string;
  imageUrl?: string;
  skills?: string;
  jobRole?: string;
  companyInfo?: string;
}

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {

  private apiUrl = `${environment.apiUrl}/api/employees`;
  private companyUrl = `${environment.apiUrl}/api/company`;

  constructor(private http: HttpClient) {}

  getUsers(role?: string): Observable<Employee[]> {
    if (role) {
      return this.http.get<Employee[]>(`${this.apiUrl}?role=${role}`);
    }
    return this.http.get<Employee[]>(this.apiUrl);
  }

  getAllEmployees(): Observable<Employee[]> {
    return this.getUsers();
  }

  getEmployeesOnly(): Observable<Employee[]> {
    return this.getUsers('EMPLOYEE');
  }

  getAdminsOnly(): Observable<Employee[]> {
    return this.getUsers('ADMIN');
  }

  getEmployeeById(id: number | string): Observable<Employee> {
    return this.http.get<Employee>(`${this.apiUrl}/${id}`);
  }

  getEmployeeByEmail(email: string): Observable<Employee> {
    return this.http.get<Employee>(`${this.apiUrl}/email/${email}`);
  }

  searchEmployees(searchTerm: string): Observable<Employee[]> {
    return this.http.get<Employee[]>(`${this.apiUrl}/search/${searchTerm}`);
  }

  addEmployee(employee: Employee): Observable<Employee> {
    return this.http.post<Employee>(this.apiUrl, employee);
  }

  updateEmployee(id: number | string, employee: Employee): Observable<Employee> {
    return this.http.put<Employee>(`${this.apiUrl}/${id}`, employee);
  }

  deleteEmployee(id: number | string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  bulkDeleteEmployees(ids: (number | string)[]): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/bulk-delete`, ids);
  }

  uploadImage(id: number | string, file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.apiUrl}/${id}/upload-image`, formData, {
      responseType: 'text'
    });
  }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
  }

  getCompany(): Observable<any> {
    return this.http.get(this.companyUrl, {
      headers: this.getAuthHeaders()
    });
  }

  saveCompany(data: any): Observable<any> {
    return this.http.post(this.companyUrl, data, {
      headers: this.getAuthHeaders()
    });
  }

  addLocation(loc: { city: string }): Observable<any> {
    return this.http.post(`${this.companyUrl}/location`, loc, {
      headers: this.getAuthHeaders()
    });
  }

  replaceLocations(locations: any[]) {
    return this.http.post(`${this.companyUrl}/locations/replace`, locations);
  }

  deleteLocation(id: number | string): Observable<any> {
    return this.http.delete(`${this.companyUrl}/location/${id}`, {
      headers: this.getAuthHeaders()
    });
  }

  uploadCompanyImage(file: File): Observable<{url: string}> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<{url: string}>(`${this.companyUrl}/upload-image`, formData, {
      headers: this.getAuthHeaders()
    });
  }
}