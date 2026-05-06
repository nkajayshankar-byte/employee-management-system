import { Component, OnInit, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { Employee, EmployeeService } from '../../services/employee';
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
  selectedEmployeeIds: string[] = [];
  isAllSelected = false;
  apiUrl = environment.apiUrl;

  constructor(
    private employeeService: EmployeeService,
    private toastr: ToastrService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private ngZone: NgZone
  ) { }

ngOnInit(): void {
  this.loadEmployees();
}

  loadEmployees(): void {
    setTimeout(() => {
      this.loading = true;
      this.cdr.detectChanges();
    }, 0);

    this.employeeService.getAllEmployees().subscribe({
      next: (data) => {
        this.ngZone.run(() => {
          this.employees = (data || []).filter(
            (user: any) => user.role === 'EMPLOYEE' || user.role === 'USER'
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

  viewDetails(id: string | undefined): void {
    if (id) {
      this.router.navigate(['/admin/employee', id]);
    }
  }

  editEmployee(id: string | undefined): void {
    if (id) {
      this.router.navigate(['/admin/edit-employee', id]);
    }
  }

  deleteEmployee(id: string | undefined): void {
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
        .filter(id => !!id) as string[];
    } else {
      this.selectedEmployeeIds = [];
    }
    this.cdr.detectChanges();
  }

  toggleEmployeeSelection(id: string | undefined): void {
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

  hireEmployee(id: string | undefined): void {
    if (!id) return;
    
    if (confirm('Are you sure you want to hire this user? Their role will be updated to EMPLOYEE.')) {
      const user = this.employees.find(e => e.id === id);
      if (user) {
        const updatedUser = { ...user, role: 'EMPLOYEE' };
        this.employeeService.updateEmployee(id, updatedUser).subscribe({
          next: () => {
            this.toastr.success('User hired successfully!');
            this.loadEmployees();
          },
          error: () => {
            this.toastr.error('Failed to hire user');
          }
        });
      }
    }
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