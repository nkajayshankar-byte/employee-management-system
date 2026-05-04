import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AssetService } from '../../services/asset';
import { EmployeeService } from '../../services/employee';

@Component({
  selector: 'app-add-asset',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './add-asset.html',
  styleUrls: ['./add-asset.css']
})
export class AddAssetComponent implements OnInit {
  assetForm: FormGroup;
  loading = false;
  employees: any[] = [];
  filteredEmployees: any[] = [];
  employeeSearchTerm = '';
  showEmployeeDropdown = false;

  assetTypes = ['Laptop', 'Monitor', 'Keyboard', 'Mouse', 'Desk Phone', 'Mobile', 'Headphones', 'Other'];
  statuses = ['ALLOCATED', 'RETURNED', 'DAMAGED', 'LOST'];
  conditions = ['Good', 'Fair', 'Poor', 'Damaged'];

  constructor(
    private fb: FormBuilder,
    private assetService: AssetService,
    private employeeService: EmployeeService,
    private router: Router,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef
  ) {
    this.assetForm = this.fb.group({
      employeeId: ['', Validators.required],
      employeeName: ['', Validators.required],
      assetName: ['', Validators.required],
      assetType: ['', Validators.required],
      serialNumber: ['', Validators.required],
      status: ['ALLOCATED', Validators.required],
      assignedDate: [new Date().toISOString().split('T')[0], Validators.required],
      returnDate: [''],
      conditions: ['Good', Validators.required],
      description: ['']
    });
  }

  ngOnInit(): void {
    this.loadEmployees();
  }

  loadEmployees(): void {
    this.employeeService.getAllEmployees().subscribe({
      next: (res: any[]) => {
        this.employees = (res || []).filter(emp => emp.role === 'EMPLOYEE');
        this.filteredEmployees = [...this.employees];
        this.cdr.detectChanges();
      },
      error: () => this.toastr.error('Failed to load employees')
    });
  }

  searchEmployees(): void {
    const term = this.employeeSearchTerm.toLowerCase().trim();
    if (!term) {
      this.filteredEmployees = [...this.employees];
    } else {
      this.filteredEmployees = this.employees.filter(emp =>
        (emp.name && emp.name.toLowerCase().includes(term)) ||
        (emp.id && emp.id.toString().includes(term))
      );
    }
    this.showEmployeeDropdown = this.filteredEmployees.length > 0;
    this.cdr.detectChanges();
  }

  selectEmployee(employee: any): void {
    this.assetForm.patchValue({
      employeeId: employee.id,
      employeeName: employee.name
    });
    this.employeeSearchTerm = employee.name;
    this.showEmployeeDropdown = false;
    this.cdr.detectChanges();
  }

  hideDropdown(): void {
    setTimeout(() => {
      this.showEmployeeDropdown = false;
      this.cdr.detectChanges();
    }, 200);
  }

  onSubmit(): void {
    if (this.assetForm.invalid) {
      this.toastr.error('Please fill all required fields');
      return;
    }

    this.loading = true;
    const f = this.assetForm.value;

    // Helper to format yyyy-MM-dd to dd-MM-yy HH:mm
    const formatDate = (dateStr: string): string => {
      if (!dateStr) return '';
      const [year, month, day] = dateStr.split('-');
      // We add 00:00 as default time for date-only inputs to match LocalDateTime expectation
      return `${day}-${month}-${year.slice(-2)} 00:00`;
    };

    const payload = {
      ...f,
      employeeId: String(f.employeeId),
      assignedDate: formatDate(f.assignedDate),
      returnDate: f.returnDate ? formatDate(f.returnDate) : null,
      remarks: ''
    };

    this.assetService.createAsset(payload).subscribe({
      next: () => {
        this.toastr.success('Asset registered successfully');
        this.router.navigate(['/admin/assets']);
      },
      error: () => {
        this.loading = false;
        this.toastr.error('Failed to register asset');
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/admin/assets']);
  }
}
