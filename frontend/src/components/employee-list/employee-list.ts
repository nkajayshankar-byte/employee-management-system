import { Component, OnInit, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { Employee, EmployeeService } from '../../services/employee';
import { CareerService, Job } from '../../services/carrerservice';
import { environment } from '../../environments/environment';
import { ChangeDetectorRef } from '@angular/core';
 import * as XLSX from 'xlsx'; // Import xlsx
@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './employee-list.html',
  styleUrls: ['./employee-list.css']
})
export class EmployeeListComponent implements OnInit {
  employees: Employee[] = [];
  filteredEmployees: Employee[] = [];
  searchTerm = '';
  loading = false;
  sortBy: 'name' | 'email' = 'name';
  selectedEmployeeIds: (string | number)[] = [];
  isAllSelected = false;
  apiUrl = environment.apiUrl;

  // Hiring Modal State
  showHireModal = false;
  selectedUserForHire: Employee | null = null;
  selectedJobRole = '';
  availableJobRoles: Job[] = [];

  constructor(
    private employeeService: EmployeeService,
    private toastr: ToastrService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private ngZone: NgZone,
    private careerService: CareerService
  ) { }

ngOnInit(): void {
  this.loadEmployees();
  this.loadJobRoles();
}

loadJobRoles(): void {
  this.careerService.getJobs().subscribe({
    next: (jobs) => {
      this.availableJobRoles = jobs.filter(j => j.isActive !== false);
      this.cdr.detectChanges();
    },
    error: () => console.error('Failed to load job roles')
  });
}

  loadEmployees(): void {
    this.loading = true;
    this.employeeService.getAllEmployees().subscribe({
      next: (data) => {
        this.ngZone.run(() => {
          this.employees = (data || []).filter(
            (user: any) => user.role === 'EMPLOYEE' || user.role === 'USER' || user.role === 'ADMIN'
          );
          this.filteredEmployees = [...this.employees];
          this.sortEmployees();
          this.loading = false;
          this.cdr.detectChanges();
        });
      },
      error: () => {
        this.ngZone.run(() => {
          this.loading = false;
          this.cdr.detectChanges();
        });
      }
    });
  }

  onSearch(): void {
    if (!this.searchTerm.trim()) {
      this.filteredEmployees = this.employees;
    } else {
      this.employeeService.searchEmployees(this.searchTerm).subscribe({
        next: (data) => {
          this.ngZone.run(() => {
            this.filteredEmployees = data;
            this.cdr.detectChanges();
          });
        },
        error: () => {
          this.ngZone.run(() => {
            this.toastr.error('Search failed');
            this.cdr.detectChanges();
          });
        }
      });
    }
  }

  sortEmployees(): void {
    this.filteredEmployees.sort((a, b) => {
      if (this.sortBy === 'name') {
        return (a.name || '').localeCompare(b.name || '');
      } else {
        return (a.email || '').localeCompare(b.email || '');
      }
    });
  }

  viewDetails(id: string | number | undefined): void {
    if (id) {
      this.router.navigate(['/admin/employee', id]);
    }
  }

  editEmployee(id: string | number | undefined): void {
    if (id) {
      this.router.navigate(['/admin/edit-employee', id]);
    }
  }

  deleteEmployee(id: string | number | undefined): void {
    if (!id) return;

    if (confirm('Are you sure you want to delete this employee?')) {
      this.employeeService.deleteEmployee(id).subscribe({
        next: () => {
          this.toastr.success('Employee deleted successfully');
          this.loadEmployees();
        },
        error: () => {
          this.toastr.error('Failed to delete employee');
        }
      });
    }
  }

  toggleSelectAll(): void {
    this.isAllSelected = !this.isAllSelected;
    if (this.isAllSelected) {
      this.selectedEmployeeIds = this.filteredEmployees
        .map(emp => emp.id)
        .filter(id => !!id) as (string | number)[];
    } else {
      this.selectedEmployeeIds = [];
    }
    this.cdr.detectChanges();
  }

  toggleEmployeeSelection(id: string | number | undefined): void {
    if (!id) return;
    const index = this.selectedEmployeeIds.indexOf(id);
    if (index > -1) {
      this.selectedEmployeeIds.splice(index, 1);
    } else {
      this.selectedEmployeeIds.push(id);
    }
    
    this.isAllSelected = this.selectedEmployeeIds.length === this.filteredEmployees.length && this.filteredEmployees.length > 0;
    this.cdr.detectChanges();
  }

  bulkDelete(): void {
    if (this.selectedEmployeeIds.length === 0) return;

    if (confirm(`Are you sure you want to delete ${this.selectedEmployeeIds.length} employees?`)) {
      this.employeeService.bulkDeleteEmployees(this.selectedEmployeeIds).subscribe({
        next: () => {
          this.toastr.success(`${this.selectedEmployeeIds.length} employees deleted successfully`);
          this.selectedEmployeeIds = [];
          this.isAllSelected = false;
          this.loadEmployees();
        },
        error: () => {
          this.toastr.error('Failed to perform bulk delete');
        }
      });
    }
  }

  hireEmployee(id: string | number | undefined): void {
    if (!id) return;
    
    const user = this.employees.find(e => e.id === id);
    if (user) {
      this.selectedUserForHire = user;
      this.selectedJobRole = '';
      this.showHireModal = true;
      this.cdr.detectChanges();
    }
  }

  confirmHire(): void {
    if (!this.selectedUserForHire || !this.selectedUserForHire.id) return;
    if (!this.selectedJobRole) {
      this.toastr.warning('Please select a job role');
      return;
    }

    const updatedUser = { 
      ...this.selectedUserForHire, 
      role: 'EMPLOYEE',
      jobRole: this.selectedJobRole 
    };

    this.employeeService.updateEmployee(this.selectedUserForHire.id, updatedUser).subscribe({
      next: () => {
        this.toastr.success('User hired successfully!');
        this.showHireModal = false;
        this.selectedUserForHire = null;
        this.loadEmployees();
      },
      error: () => {
        this.toastr.error('Failed to hire user');
      }
    });
  }

  promoteToAdmin(employee: Employee): void {
    if (!employee || !employee.id) return;

    if (confirm(`Are you sure you want to promote ${employee.name} to Administrator? This will grant them full access to the system.`)) {
      const updatedUser = { ...employee, role: 'ADMIN' };
      this.employeeService.updateEmployee(employee.id, updatedUser).subscribe({
        next: () => {
          this.toastr.success(`${employee.name} promoted to Admin successfully!`);
          this.loadEmployees();
        },
        error: () => this.toastr.error('Failed to promote user')
      });
    }
  }

  closeHireModal(): void {
    this.showHireModal = false;
    this.selectedUserForHire = null;
    this.cdr.detectChanges();
  }

  exportToExcel(): void {
      if (this.filteredEmployees.length === 0) {
        this.toastr.warning('No data to export');
        return;
      }

      // Map the data to remove internal IDs and format the excel columns
      const dataToExport = this.filteredEmployees.map(emp => ({
        'Name': emp.name,
        'Email': emp.email,
        'Mobile': emp.mobile || 'N/A',
        'Address': emp.address || 'N/A'
      }));

      const worksheet = XLSX.utils.json_to_sheet(dataToExport);
      const workbook = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(workbook, worksheet, 'Employees');

      // Generate file and trigger download
      XLSX.writeFile(workbook, 'Employee_Directory.xlsx');
      this.toastr.success('Exported to Excel successfully');
    }
}