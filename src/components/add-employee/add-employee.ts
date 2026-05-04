import { ChangeDetectorRef, Component, HostListener, OnInit } from '@angular/core';
  import { CommonModule } from '@angular/common';
  import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup,
  Validators } from '@angular/forms';
  import { Router } from '@angular/router';
  import { ToastrService } from 'ngx-toastr';
  import { Employee, EmployeeService } from '../../services/employee';
import { CareerService, Job } from '../../services/carrerservice';
  import { HasUnsavedChanges } from '../../services/unsaved-changes.guard';

  @Component({
    selector: 'app-add-employee',
    standalone: true,
    imports: [CommonModule, FormsModule, ReactiveFormsModule],
    templateUrl: './add-employee.html',
    styleUrls: ['./add-employee.css']
  })
  export class AddEmployeeComponent implements OnInit, HasUnsavedChanges {

    form: FormGroup;
    loading = false;
    selectedFile: File | null = null;
    employeeId: string | null = null;
    imagePreview: string | null = null;
    availableJobRoles: Job[] = [];

    constructor(
      private fb: FormBuilder,
      private employeeService: EmployeeService,
      private router: Router,
      private toastr: ToastrService,
      private cd: ChangeDetectorRef,
      private careerService: CareerService
    ) {
      this.form = this.fb.group({
        // Email: Required, must contain @ and .com
        email: ['', [
          Validators.required,
          Validators.pattern(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.com$/)
        ]],
        // Name: Required, min 2 chars
        name: ['', [Validators.required, Validators.minLength(2)]],
        // Mobile: Exactly 10 digits
        mobile: ['', [Validators.pattern(/^[0-9]{10}$/)]],
        address: [''],
        skills: [''],
        companyInfo: [''],
        imageUrl: [''],
        jobRole: ['']
      });
    }

    ngOnInit(): void {
      this.careerService.getJobs().subscribe({
        next: (jobs) => {
          this.availableJobRoles = jobs;
          this.cd.detectChanges();
        },
        error: () => console.error('Failed to load job roles')
      });
    }

    // Helper method to check if a field is invalid in the template
    isInvalid(controlName: string): boolean {
      const control = this.form.get(controlName);
      return !!(control && control.invalid && (control.touched ||
  control.dirty));
    }

    onFileSelected(event: any): void {
      const file = event.target.files?.[0];
      if (file) {
        this.selectedFile = file;
        
        // Generate preview
        const reader = new FileReader();
        reader.onload = (e: any) => {
          this.imagePreview = e.target.result;
          this.cd.detectChanges();
        };
        reader.readAsDataURL(file);
      }
    }

    onSubmit(): void {
      if (this.form.invalid) {
        this.form.markAllAsTouched(); // Trigger all validation pop-ups
        this.toastr.error('Please fix the errors in the form');
        return;
      }

      this.loading = true;

      const employee: Employee = {
        ...this.form.value,
        role: 'EMPLOYEE'
      };

      this.employeeService.addEmployee(employee).subscribe({
        next: (savedEmployee) => {
          this.form.markAsPristine(); // Reset dirty state
          this.employeeId = savedEmployee.id ?? null;

          if (this.selectedFile && this.employeeId) {
            this.uploadImageAfterSave();
          } else {
            this.toastr.success('Employee added successfully');
            this.router.navigate(['/admin/employees']);
          }
          this.loading = false;
          this.cd.detectChanges();
        },
        error: () => {
          this.loading = false;
          this.toastr.error('Failed to add employee');
        }
      });
    }

    uploadImageAfterSave(): void {
      if (!this.employeeId || !this.selectedFile) return;

      this.employeeService.uploadImage(this.employeeId,
  this.selectedFile).subscribe({
        next: () => {
          this.toastr.success('Employee added with image');
          this.router.navigate(['/admin/employees']);
        },
        error: () => {
          this.toastr.error('Image upload failed, but employee saved');
          this.router.navigate(['/admin/employees']);
        }
      });
    }

    onCancel(): void {
      this.router.navigate(['/admin/employees']);
    }

    @HostListener('window:beforeunload', ['$event'])
    unloadNotification($event: any): void {
      if (this.hasUnsavedChanges()) {
        $event.returnValue = true;
      }
    }

    hasUnsavedChanges(): boolean {
      return this.form.dirty && !this.loading;
    }
  }