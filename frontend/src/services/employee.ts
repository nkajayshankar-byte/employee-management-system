import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Employee {
  id?: string;
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

  private apiUrl = 'http://localhost:8080/api/employees';
  private companyUrl = 'http://localhost:8080/api/company';

  constructor(private http: HttpClient) {}

  // ✅ GET USERS (ALL / FILTERED BY ROLE)
  getUsers(role?: string): Observable<Employee[]> {
    if (role) {
      return this.http.get<Employee[]>(`${this.apiUrl}?role=${role}`);
    }
    return this.http.get<Employee[]>(this.apiUrl);
  }

  // ✅ OPTIONAL (BACKWARD SUPPORT)
  getAllEmployees(): Observable<Employee[]> {
    return this.getUsers(); // same as all users
  }

  getEmployeesOnly(): Observable<Employee[]> {
    return this.getUsers('EMPLOYEE');
  }

  getAdminsOnly(): Observable<Employee[]> {
    return this.getUsers('ADMIN');
  }

  // ✅ GET BY ID
  getEmployeeById(id: string): Observable<Employee> {
    return this.http.get<Employee>(`${this.apiUrl}/${id}`);
  }

  // ✅ GET BY EMAIL
  getEmployeeByEmail(email: string): Observable<Employee> {
    return this.http.get<Employee>(`${this.apiUrl}/email/${email}`);
  }

  // ✅ SEARCH
  searchEmployees(searchTerm: string): Observable<Employee[]> {
    return this.http.get<Employee[]>(`${this.apiUrl}/search/${searchTerm}`);
  }

  // ✅ ADD
  addEmployee(employee: Employee): Observable<Employee> {
    return this.http.post<Employee>(this.apiUrl, employee);
  }

  // ✅ UPDATE
  updateEmployee(id:string, employee: Employee): Observable<Employee> {
    return this.http.put<Employee>(`${this.apiUrl}/${id}`, employee);
  }

  // ✅ DELETE
  deleteEmployee(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // ✅ BULK DELETE
  bulkDeleteEmployees(ids: string[]): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/bulk-delete`, ids);
  }

  // ✅ UPLOAD IMAGE
  uploadImage(id: string, file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post(`${this.apiUrl}/${id}/upload-image`, formData, {
      responseType: 'text'
    });
  }

  // 🔐 AUTH HEADERS
  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
  }

  // ✅ COMPANY APIs
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

  deleteLocation(id: string): Observable<any> {
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