import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { PayrollService, SalaryStructure, Payslip } from '../../services/payroll.service';
import { EmployeeService, Employee } from '../../services/employee';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-admin-payroll',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-payroll.html',
  styleUrls: ['./admin-payroll.css']
})
export class AdminPayrollComponent implements OnInit {
  apiUrl = environment.apiUrl;
  employees: Employee[] = [];
  searchTerm: string = '';
  selectedEmployee: Employee | null = null;

  get filteredEmployees(): Employee[] {
    if (!this.searchTerm.trim()) return this.employees;
    const term = this.searchTerm.toLowerCase().trim();
    return this.employees.filter(emp =>
      (emp.name?.toLowerCase().includes(term)) ||
      (emp.jobRole?.toLowerCase().includes(term))
    );
  }
  salaryStructure: SalaryStructure = {
    employeeId: 0,
    baseSalary: 0,
    hra: 0,
    otherAllowances: 0,
    taxDeductions: 0,
    providentFund: 0,
    accountNumber: ''
  };
  
  payslips: Payslip[] = [];
  
  // For Payslip Generation
  generateMonth: number = new Date().getMonth() + 1;
  generateYear: number = new Date().getFullYear();
  isGenerating: boolean = false;
  months = [
    { value: 1, name: 'January' }, { value: 2, name: 'February' },
    { value: 3, name: 'March' }, { value: 4, name: 'April' },
    { value: 5, name: 'May' }, { value: 6, name: 'June' },
    { value: 7, name: 'July' }, { value: 8, name: 'August' },
    { value: 9, name: 'September' }, { value: 10, name: 'October' },
    { value: 11, name: 'November' }, { value: 12, name: 'December' }
  ];

  constructor(
    private payrollService: PayrollService,
    private employeeService: EmployeeService,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadEmployees();
  }

  loadEmployees(): void {
    this.employeeService.getAllEmployees().subscribe({
      next: (data) => {
        this.employees = (data || []).filter(e => e.role === 'EMPLOYEE');
        this.cdr.detectChanges();
      },
      error: () => this.toastr.error('Failed to load employees')
    });
  }

  selectEmployee(employee: Employee): void {
    this.selectedEmployee = employee;
    if (employee.id) {
      this.loadSalaryStructure(employee.id as number);
      this.loadPayslips(employee.id as number);
    }
  }

  loadSalaryStructure(employeeId: number): void {
    this.payrollService.getSalaryStructure(employeeId).subscribe({
      next: (data) => {
        if (data) {
          this.salaryStructure = data;
          if (!this.salaryStructure.accountNumber) {
            this.salaryStructure.accountNumber = '';
          }
        } else {
          this.resetSalaryStructure(employeeId);
        }
        this.cdr.detectChanges();
      },
      error: () => {
        this.resetSalaryStructure(employeeId);
        this.cdr.detectChanges();
      }
    });
  }

  resetSalaryStructure(employeeId: number): void {
    this.salaryStructure = {
      employeeId: employeeId,
      baseSalary: 0,
      hra: 0,
      otherAllowances: 0,
      taxDeductions: 0,
      providentFund: 0,
      accountNumber: ''
    };
  }

  loadPayslips(employeeId: number): void {
    this.payrollService.getEmployeePayslips(employeeId).subscribe({
      next: (data) => {
        this.payslips = data || [];
        this.cdr.detectChanges();
      },
      error: () => this.toastr.error('Failed to load payslips')
    });
  }

  saveSalaryStructure(): void {
    if (!this.selectedEmployee || !this.selectedEmployee.id) return;
    
    if (!this.salaryStructure.baseSalary || this.salaryStructure.baseSalary <= 0) {
      this.toastr.error('Base Salary cannot be empty or 0.');
      return;
    }

    this.salaryStructure.employeeId = this.selectedEmployee.id as number;
    this.payrollService.saveSalaryStructure(this.salaryStructure).subscribe({
      next: (data) => {
        this.salaryStructure = data;
        this.toastr.success('Salary structure saved successfully!');
        this.cdr.detectChanges();
      },
      error: () => this.toastr.error('Failed to save salary structure')
    });
  }

  generatePayslip(): void {
    if (!this.selectedEmployee || !this.selectedEmployee.id) return;

    this.isGenerating = true;
    this.payrollService.generatePayslip(this.selectedEmployee.id as number, this.generateMonth, this.generateYear).subscribe({
      next: (data) => {
        this.toastr.success('Payslip generated successfully!');
        this.loadPayslips(this.selectedEmployee!.id as number);
        this.isGenerating = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.toastr.error('Failed to generate payslip');
        this.isGenerating = false;
        this.cdr.detectChanges();
      }
    });
  }

  getFileUrl(url: string | undefined): string {
    if (!url) return '';
    if (url.startsWith('/api/files/download/payslips/')) {
      return `${environment.apiUrl}/uploads/payslips/${url.substring('/api/files/download/payslips/'.length)}`;
    }
    if (url.startsWith('/uploads')) {
      return `${environment.apiUrl}${url}`;
    }
    return url;
  }
}
