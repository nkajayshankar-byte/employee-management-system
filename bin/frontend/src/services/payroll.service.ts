import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface SalaryStructure {
  id?: number;
  employeeId: number;
  baseSalary: number;
  hra: number;
  otherAllowances: number;
  taxDeductions: number;
  providentFund: number;
  netSalary?: number;
  accountNumber?: string;
}

export interface Payslip {
  id?: number;
  employeeId: number;
  month: number;
  year: number;
  totalDays: number;
  paidDays: number;
  grossPay: number;
  totalDeductions: number;
  lopAmount?: number;
  netPay: number;
  status: string;
  pdfUrl?: string;
  employeeName?: string;
  employeeEmail?: string;
}

@Injectable({
  providedIn: 'root'
})
export class PayrollService {
  private apiUrl = `${environment.apiUrl}/api/payroll`;

  constructor(private http: HttpClient) {}

  saveSalaryStructure(salaryStructure: SalaryStructure): Observable<SalaryStructure> {
    return this.http.post<SalaryStructure>(`${this.apiUrl}/salary`, salaryStructure);
  }

  getSalaryStructure(employeeId: number): Observable<SalaryStructure> {
    return this.http.get<SalaryStructure>(`${this.apiUrl}/salary/${employeeId}`);
  }

  generatePayslip(employeeId: number, month: number, year: number): Observable<Payslip> {
    const params = new HttpParams().set('month', month).set('year', year);
    return this.http.post<Payslip>(`${this.apiUrl}/generate/${employeeId}`, null, { params });
  }

  getEmployeePayslips(employeeId: number): Observable<Payslip[]> {
    return this.http.get<Payslip[]>(`${this.apiUrl}/payslips/${employeeId}`);
  }
}
