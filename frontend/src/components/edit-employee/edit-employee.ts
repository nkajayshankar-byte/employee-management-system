import { ChangeDetectorRef, Component, OnInit, HostListener, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { Employee, EmployeeService } from '../../services/employee';
import { environment } from '../../environments/environment';
import { CareerService, Job } from '../../services/carrerservice';
import { HasUnsavedChanges } from '../../services/unsaved-changes.guard';

@Component({
  selector: 'app-edit-employee',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './edit-employee.html',
  styleUrls: ['./edit-employee.css']
})
export class EditEmployeeComponent implements OnInit, HasUnsavedChanges {
  form: FormGroup;
  loading = false;
  submitting = false;
  apiUrl = environment.apiUrl;
  employeeId: string | null = null;
  selectedFile: File | null = null;
  imagePreview: string | null = null;
  availableJobRoles: Job[] = [];

  constructor(
    private fb: FormBuilder,
    private employeeService: EmployeeService,
    private route: ActivatedRoute,
    private router: Router,
    private toastr: ToastrService,
    private cd: ChangeDetectorRef,
    private ngZone: NgZone,
    private careerService: CareerService
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      name: ['', [Validators.required, Validators.minLength(2)]],
      mobile: ['', [Validators.pattern(/^[0-9]{10}$/)]],
      address: [''],
      skills: [''],
      companyInfo: [''],
      imageUrl: [''],
      role: ['EMPLOYEE'],
      jobRole: ['']
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];
    this.employeeId = id;
    this.loadEmployee(id);
    this.careerService.getJobs().subscribe({
      next: (jobs) => {
        this.availableJobRoles = jobs;
        this.cd.detectChanges();
      },
      error: () => console.error('Failed to load job roles')
    });
  }

  loadEmployee(id: string): void {
    setTimeout(() => {
      this.loading = true;
      this.cd.detectChanges();
    }, 0);

    this.employeeService.getEmployeeById(id).subscribe({
      next: (employee) => {
        this.ngZone.run(() => {
          this.form.patchValue(employee);
          this.loading = false;
          this.cd.detectChanges();
        });
      },
      error: (err) => {
        this.ngZone.run(() => {
          this.loading = false;
          this.toastr.error('Failed to load employee');
          this.cd.detectChanges();
        });
      }
    });
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

  uploadImage(): void {
    if (this.employeeId && this.selectedFile) {
      this.employeeService.uploadImage(this.employeeId, this.selectedFile).subscribe({
        next: (url: string) => {
          this.ngZone.run(() => {
            this.toastr.success('Image uploaded successfully');
            this.form.patchValue({ imageUrl: url });
            this.selectedFile = null;
            this.cd.detectChanges();
          });
        },
        error: (err) => {
          this.ngZone.run(() => {
            this.toastr.error('Failed to upload image');
            this.cd.detectChanges();
          });
        }
      });
    }
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.toastr.error('Please fill all required fields');
      return;
    }

    this.ngZone.run(() => {
      this.submitting = true;
      this.cd.detectChanges();
    });
    const employee: Employee = { ...this.form.value };

    if (this.employeeId) {
      this.employeeService.updateEmployee(this.employeeId, employee).subscribe({
        next: () => {
          this.ngZone.run(() => {
            this.form.markAsPristine();
            this.submitting = false;
            this.toastr.success('Employee updated successfully');
            this.router.navigate(['/admin/employees']);
            this.cd.detectChanges();
          });
        },
        error: () => {
          this.ngZone.run(() => {
            this.submitting = false;
            this.toastr.error('Failed to update employee');
            this.cd.detectChanges();
          });
        }
      });
    }
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
    return this.form.dirty && !this.submitting;
  }
}