import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CareerService } from '../../services/carrerservice';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-job-application-form',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './job-application-form.html',
  styleUrls: ['./job-application-form.css']
})
export class JobApplicationFormComponent implements OnInit {
  @Input() jobId!: string;
  @Input() jobTitle!: string;
  @Output() close = new EventEmitter<void>();
  @Output() success = new EventEmitter<void>();

  currentStep = 1;
  totalSteps = 3;
  applicationForm!: FormGroup;
  uploading = false;
  resumeUrl: string = '';

  constructor(
    private fb: FormBuilder,
    private careerService: CareerService,
    private toastr: ToastrService
  ) {
    this.applicationForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', Validators.required],
      skills: ['', Validators.required],
      experience: ['', Validators.required],
      resume: [null, Validators.required]
    });
  }

  ngOnInit(): void {
    const user = JSON.parse(localStorage.getItem('currentUser') || '{}');
    this.applicationForm.patchValue({
      name: user.name || '',
      email: user.email || '',
      phone: user.mobile || ''
    });
  }

  nextStep(): void {
    if (this.currentStep === 1) {
      const controls = ['name', 'email', 'phone'];
      const invalid = controls.some(c => this.applicationForm.get(c)?.invalid);
      if (invalid) {
        controls.forEach(c => this.applicationForm.get(c)?.markAsTouched());
        this.toastr.error('Please fill in your contact details correctly.');
        return;
      }
    } else if (this.currentStep === 2) {
      const controls = ['skills', 'experience'];
      const invalid = controls.some(c => this.applicationForm.get(c)?.invalid);
      if (invalid) {
        controls.forEach(c => this.applicationForm.get(c)?.markAsTouched());
        this.toastr.error('Please provide your skills and experience.');
        return;
      }
    } else if (this.currentStep === 3 && !this.resumeUrl) {
      this.applicationForm.get('resume')?.markAsTouched();
      this.toastr.warning('Please upload your resume to complete the application');
      return;
    }

    if (this.currentStep < this.totalSteps) {
      this.currentStep++;
    }
  }

  prevStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
    }
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        this.toastr.error('File size exceeds 5MB limit');
        return;
      }
      
      const allowedTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
      if (!allowedTypes.includes(file.type)) {
        this.toastr.error('Only PDF and Word documents are allowed');
        return;
      }

      // Fix: Wrap in setTimeout to avoid ExpressionChangedAfterItHasBeenCheckedError
      setTimeout(() => {
        this.uploading = true;
        this.careerService.uploadResume(file).subscribe({
          next: (res: any) => {
            this.resumeUrl = res.url;
            this.applicationForm.patchValue({ resume: res.url });
            this.applicationForm.get('resume')?.setErrors(null);
            this.uploading = false;
            this.toastr.success('Resume uploaded successfully');
          },
          error: (err) => {
            this.toastr.error('Failed to upload resume');
            this.uploading = false;
          }
        });
      });
    }
  }

  submitApplication(): void {
    // Explicit Resume Check
    if (!this.resumeUrl) {
      this.applicationForm.get('resume')?.setErrors({ required: true });
      this.applicationForm.get('resume')?.markAsTouched();
      this.toastr.error('Resume is required. Please upload your CV before submitting.');
      return;
    }

    // General Form Validity Check
    if (this.applicationForm.invalid) {
      this.applicationForm.markAllAsTouched();
      this.toastr.error('Please complete all required fields across all steps.');
      
      // If there are errors in previous steps, take them back to step 1 or 2
      const step1Fields = ['name', 'email', 'phone'];
      const step2Fields = ['skills', 'experience'];
      
      if (step1Fields.some(f => this.applicationForm.get(f)?.invalid)) {
        this.currentStep = 1;
      } else if (step2Fields.some(f => this.applicationForm.get(f)?.invalid)) {
        this.currentStep = 2;
      }
      
      return;
    }

    const user = JSON.parse(localStorage.getItem('currentUser') || '{}');
    const empId = user.userId || user.id || user._id; // Be flexible with user ID

    if (!empId) {
      this.toastr.error('User session not found. Please log in again.');
      return;
    }

    const actualJobId = this.jobId || (this as any)._jobId; // Fallback check

    if (!actualJobId) {
      this.toastr.error('Job reference missing. Please close and reopen the application.');
      return;
    }

    const applicationData = {
      jobId: actualJobId,
      employeeId: empId,
      employeeName: this.applicationForm.value.name,
      employeeEmail: this.applicationForm.value.email,
      resumeUrl: this.resumeUrl,
      status: 'PENDING'
    };



    this.careerService.apply(applicationData).subscribe({
      next: (res) => {

        this.toastr.success('Application submitted successfully!');
        this.success.emit();
      },
      error: (err) => {
        const errorMsg = err.error?.message || 'Failed to submit application. Please try again.';
        this.toastr.error(errorMsg);
      }
    });
  }

  // Helper to debug validation
  private getFormValidationErrors() {
    const errors: any[] = [];
    Object.keys(this.applicationForm.controls).forEach(key => {
      const controlErrors: any = this.applicationForm.get(key)?.errors;
      if (controlErrors != null) {
        errors.push({ key, errors: controlErrors });
      }
    });
    return errors;
  }
}
