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
  submitting = false;
  resumeUrl: string = '';
  fileName: string = '';
  
  // Store AI screening data to send during submit
  aiScreeningData: any = null;

  constructor(
    private fb: FormBuilder,
    private careerService: CareerService,
    private toastr: ToastrService
  ) {
    this.applicationForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern('^[+0-9]{10,15}$')]],
      linkedInUrl: [''],
      githubUrl: [''],
      skills: ['', Validators.required],
      experience: ['', Validators.required],
      education: [''],
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
      if (!this.resumeUrl) {
        this.toastr.warning('Please upload your resume to proceed.');
        return;
      }
    } else if (this.currentStep === 2) {
      const controls = ['name', 'email', 'phone'];
      const invalid = controls.some(c => this.applicationForm.get(c)?.invalid);
      if (invalid) {
        controls.forEach(c => this.applicationForm.get(c)?.markAsTouched());
        this.toastr.error('Please fill in your personal details correctly.');
        return;
      }
    } else if (this.currentStep === 3) {
      const controls = ['skills', 'experience'];
      const invalid = controls.some(c => this.applicationForm.get(c)?.invalid);
      if (invalid) {
        controls.forEach(c => this.applicationForm.get(c)?.markAsTouched());
        this.toastr.error('Please provide your skills and experience.');
        return;
      }
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

      this.fileName = file.name;
      this.uploading = true;
      const actualJobId = this.jobId || (this as any)._jobId;
      
      this.careerService.parseResume(file, actualJobId).subscribe({
        next: (res: any) => {
          this.resumeUrl = res.url;
          this.applicationForm.patchValue({ resume: res.url });
          this.applicationForm.get('resume')?.setErrors(null);
          
          if (res.analysis) {
            this.aiScreeningData = res.analysis;
            
            // Auto-fill form fields
            this.applicationForm.patchValue({
              name: res.analysis.extractedName || this.applicationForm.value.name,
              email: res.analysis.extractedEmail || this.applicationForm.value.email,
              phone: res.analysis.extractedPhone || this.applicationForm.value.phone,
              linkedInUrl: res.analysis.extractedLinkedIn || '',
              githubUrl: res.analysis.extractedGitHub || '',
              skills: res.analysis.extractedSkills || '',
              experience: res.analysis.extractedExperience || '',
              education: res.analysis.extractedEducation || ''
            });
            this.toastr.success('Resume parsed successfully! Details auto-filled.');
          } else {
             this.toastr.success('Resume uploaded successfully.');
          }
          
          this.uploading = false;
          // Automatically go to next step
          setTimeout(() => this.nextStep(), 1500);
        },
        error: (err) => {
          this.toastr.error('Failed to upload/parse resume. You can still enter details manually.');
          this.uploading = false;
          // In case of error, we could still let them upload without parsing, but parseResume endpoint handles upload too.
        }
      });
    }
  }

  submitApplication(): void {
    if (this.applicationForm.invalid || !this.resumeUrl) {
      this.applicationForm.markAllAsTouched();
      this.toastr.error('Please complete all required fields.');
      return;
    }

    const user = JSON.parse(localStorage.getItem('currentUser') || '{}');
    const empId = user.userId || user.id || user._id;

    if (!empId) {
      this.toastr.error('User session not found. Please log in again.');
      return;
    }

    const actualJobId = this.jobId || (this as any)._jobId;

    const applicationData: any = {
      jobId: actualJobId,
      employeeId: empId,
      employeeName: this.applicationForm.value.name,
      employeeEmail: this.applicationForm.value.email,
      employeePhone: this.applicationForm.value.phone,
      linkedInUrl: this.applicationForm.value.linkedInUrl,
      githubUrl: this.applicationForm.value.githubUrl,
      skills: this.applicationForm.value.skills,
      experience: this.applicationForm.value.experience,
      education: this.applicationForm.value.education,
      resumeUrl: this.resumeUrl,
      status: 'PENDING'
    };

    // Attach AI screening data if available
    if (this.aiScreeningData) {
      applicationData.matchPercentage = this.aiScreeningData.matchPercentage;
      applicationData.missingSkills = this.aiScreeningData.missingSkills?.join(', ') || '';
      applicationData.strengths = this.aiScreeningData.strengths?.join(', ') || '';
      applicationData.summary = this.aiScreeningData.summary;
      applicationData.extractedSkills = this.aiScreeningData.extractedSkills;
      applicationData.extractedExperience = this.aiScreeningData.extractedExperience;
      applicationData.extractedEducation = this.aiScreeningData.extractedEducation;
    }

    this.submitting = true;

    this.careerService.apply(applicationData).subscribe({
      next: (res) => {
        this.submitting = false;
        this.toastr.success('Application submitted successfully!');
        this.success.emit();
      },
      error: (err) => {
        this.submitting = false;
        const errorMsg = err.error?.message || 'Failed to submit application. Please try again.';
        this.toastr.error(errorMsg);
      }
    });
  }
}

