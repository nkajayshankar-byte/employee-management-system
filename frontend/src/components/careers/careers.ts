import { Component, OnInit, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { environment } from '../../environments/environment';
import { CareerService } from '../../services/carrerservice';
import { EmployeeService } from '../../services/employee';
import { ActivatedRoute, Router } from '@angular/router';
import { JobApplicationFormComponent } from '../job-application-form/job-application-form';

@Component({
  selector: 'app-careers',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, JobApplicationFormComponent],
  templateUrl: './careers.html',
  styleUrls: ['./careers.css']
})
export class CareersComponent implements OnInit {
  jobs: any[] = [];
  allJobs: any[] = [];
  applicants: any[] = [];
  myApplications: any[] = [];   
  appliedJobs: Set<string> = new Set();
  isAdmin = false;
  showJobForm = false;
  jobForm!: FormGroup;
  selectedJobForApplicants: any = null;
  applicantSearchQuery = '';
  selectedJobForApplication: any = null;
  selectedJobForDetails: any = null; // Amazon-style detail view
  userId: string = '';
  showMyApplications = false;
  apiUrl = environment.apiUrl;
  expandedAppId: string | null = null;

  constructor(
    private careerService: CareerService,
    private fb: FormBuilder,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef,
    private route: ActivatedRoute,
    private router: Router,
    private ngZone: NgZone,
    private employeeService: EmployeeService
  ) {
    this.jobForm = this.fb.group({
      title: ['', Validators.required],
      department: ['', Validators.required],
      location: ['', Validators.required],
      type: ['Full-time', Validators.required],
      description: ['', Validators.required],
      keyResponsibilities: ['', Validators.required], // New field
      minSalary: [null, [Validators.required, Validators.min(0)]],
      maxSalary: [null, [Validators.required, Validators.min(0)]],
      requiredSkills: ['', Validators.required]
    });
  }

  // Filter State
  filters = {
    query: '',
    minSalary: 0,
    location: ''
  };

  userSkills: string[] = [];
  userAddress: string = '';

  onSearch(): void {
    this.careerService.searchJobs(this.filters.query, this.filters.minSalary, this.filters.location).subscribe(res => {
      this.jobs = res;
      this.cdr.detectChanges();
    });
  }

  loadJobs(): void {
    // Load user skills/address for matching
    const currentUser = JSON.parse(localStorage.getItem('currentUser') || '{}');
    this.userSkills = [];
    this.userAddress = '';

    if (currentUser.email) {
      this.employeeService.getEmployeeByEmail(currentUser.email).subscribe(emp => {
        if (emp.skills) {
          this.userSkills = emp.skills.split(',').map((s: string) => s.trim().toLowerCase()).filter((s: string) => !!s);
        }
        this.userAddress = (emp.address || '').trim().toLowerCase();
        
        // Refresh matches after getting user data
        if (this.jobs.length > 0) {
          this.jobs.sort((a, b) => this.getMatchPercentage(b) - this.getMatchPercentage(a));
          this.cdr.detectChanges();
        }
      });
    }

    this.careerService.getJobs().subscribe(res => {
      this.allJobs = res;
      // Filter for active jobs and sort by match percentage for users
      let filteredJobs = res.filter((j: any) => j.isActive !== false);
      
      if (!this.isAdmin) {
        filteredJobs.sort((a, b) => this.getMatchPercentage(b) - this.getMatchPercentage(a));
      }
      
      this.jobs = filteredJobs;
      this.cdr.detectChanges();
    });
  }

  /**
   * Smart fuzzy skill matching:
   * Strips version numbers, special chars, and checks if the core skill name overlaps.
   * e.g. "java8+" matches "Java", "python3" matches "Python", "reactjs" matches "React"
   */
  private normalizeSkill(skill: string): string {
    return skill.toLowerCase().replace(/[^a-z]/g, ''); // strip numbers, +, #, etc → "java8+" → "java"
  }

  private skillsMatch(userSkill: string, jobSkill: string): boolean {
    const normUser = this.normalizeSkill(userSkill);
    const normJob = this.normalizeSkill(jobSkill);
    if (!normUser || !normJob) return false;
    // Either one contains the other: "java" in "javascript" won't match (len diff too big)
    // But "java" in "java" or "react" in "reactjs" will
    if (normUser === normJob) return true;
    if (normUser.length >= 3 && normJob.length >= 3) {
      // The shorter one must be at least 60% the length of the longer to avoid "go" matching "golang"
      const shorter = normUser.length <= normJob.length ? normUser : normJob;
      const longer = normUser.length > normJob.length ? normUser : normJob;
      if (shorter.length / longer.length >= 0.4 && longer.includes(shorter)) return true;
    }
    return false;
  }

  getMatchPercentage(job: any): number {
    let required: any[] = [];
    if (Array.isArray(job.requiredSkills)) {
      required = job.requiredSkills;
    } else if (typeof job.requiredSkills === 'string') {
      required = job.requiredSkills.split(',').map((s: string) => s.trim());
    }

    // 1. Skills Matching (80% weight) — fuzzy
    let skillsMatchScore = 0;
    if (required.length === 0) {
      skillsMatchScore = 100;
    } else if (this.userSkills && this.userSkills.length > 0) {
      const matches = required.filter((jobSkill: string) => {
        if (!jobSkill) return false;
        return this.userSkills.some(us => this.skillsMatch(us, jobSkill));
      }).length;
      skillsMatchScore = (matches / required.length) * 100;
    }

    // 2. Location Matching (20% weight)
    let locationMatchScore = 0;
    const jobLoc = (job.location || '').toLowerCase().trim();

    if (jobLoc === 'remote' || !jobLoc) {
      locationMatchScore = 100;
    } else if (this.userAddress) {
      const jobWords = jobLoc.split(/[\s,]+/).filter((w: string) => w.length > 2);
      const userWords = this.userAddress.split(/[\s,]+/).filter((w: string) => w.length > 2);
      const hasWordMatch = jobWords.some((jw: string) => userWords.some((uw: string) => jw === uw)) ||
                           jobLoc.includes(this.userAddress) ||
                           this.userAddress.includes(jobLoc);
      if (hasWordMatch) locationMatchScore = 100;
    }

    const finalMatch = (skillsMatchScore * 0.8) + (locationMatchScore * 0.2);
    return Math.round(finalMatch);
  }

  ngOnInit(): void {
    const user = JSON.parse(localStorage.getItem('currentUser') || '{}');
    this.isAdmin = user.role === 'ADMIN';
    this.userId = user.userId || user.id;
    this.loadJobs();

    if (!this.isAdmin) {
      this.loadMyApplications();
    }

    this.route.queryParams.subscribe(params => {
      if (params['jobId'] && this.isAdmin) {
        // We wait for allJobs to be loaded by loadJobs() first
        const checkJobs = () => {
          if (this.allJobs && this.allJobs.length > 0) {
            const targetJob = this.allJobs.find((j: any) => String(j.id) === String(params['jobId']));
            if (targetJob) {
              this.ngZone.run(() => {
                this.viewApplicants(targetJob);
              });
            }
          } else {
            setTimeout(checkJobs, 100);
          }
        };
        checkJobs();
      }
    });
  }

  loadMyApplications(): void {
    if (!this.userId) return;
    this.careerService.getMyApplications(this.userId).subscribe({
      next: (res) => {
        this.ngZone.run(() => {
          this.myApplications = res;
          // Pre-populate appliedJobs set to disable buttons for existing applications
          this.appliedJobs = new Set(res.map((app: any) => app.jobId));
          this.cdr.detectChanges();
        });
      },
      error: () => this.toastr.error('Failed to load your applications')
    });
  }

  openAddForm(): void {
    this.selectedJobForEdit = null; 
    this.showJobForm = true;
    this.jobForm.reset({
      title: '',
      department: '',
      location: '',
      type: 'Full-time',
      description: '',
      keyResponsibilities: '',
      minSalary: 0,
      maxSalary: 0,
      requiredSkills: ''
    });
    this.cdr.detectChanges();
  }

  selectedJobForEdit: any = null;
  editJob(job: any): void {
    this.showJobForm = true;
    this.jobForm.patchValue({
      title: job.title,
      department: job.department,
      location: job.location,
      type: job.type,
      description: job.description,
      keyResponsibilities: job.keyResponsibilities || '',
      minSalary: job.minSalary,
      maxSalary: job.maxSalary,
      requiredSkills: (job.requiredSkills || []).join(', ')
    });
    this.selectedJobForEdit = job;
    this.cdr.detectChanges();
  }

  saveJob(): void {
    if (this.jobForm.invalid) {
      this.toastr.error('Please fill all required fields correctly');
      this.jobForm.markAllAsTouched();
      return;
    }

    const jobData = { 
      ...this.jobForm.value,
      isActive: true 
    };
    if (jobData.requiredSkills && typeof jobData.requiredSkills === 'string') {
      jobData.requiredSkills = jobData.requiredSkills.split(',').map((s: string) => s.trim()).filter((s: string) => !!s);
    }

    this.careerService.saveJob(jobData).subscribe({
      next: () => {
        this.toastr.success('Job posted successfully');
        this.showJobForm = false;
        this.loadJobs();
        this.cdr.detectChanges();
      },
      error: () => this.toastr.error('Failed to publish job')
    });
  }

  saveEdit(): void {
    if (this.jobForm.invalid) {
      this.toastr.error('Please fill all required fields correctly');
      return;
    }

    const updatedJobData = {
      ...this.jobForm.value,
      id: this.selectedJobForEdit.id 
    };

    if (updatedJobData.requiredSkills && typeof updatedJobData.requiredSkills === 'string') {
      updatedJobData.requiredSkills = updatedJobData.requiredSkills.split(',').map((s: string) => s.trim()).filter((s: string) => !!s);
    }

    this.careerService.updateJob(this.selectedJobForEdit.id, updatedJobData).subscribe({
      next: () => {
        this.toastr.success('Job updated successfully');
        this.showJobForm = false;
        this.selectedJobForEdit = null;
        this.loadJobs();
        this.cdr.detectChanges();
      },
      error: () => this.toastr.error('Failed to update job')
    });
  }

  deleteJob(id: string): void {
    if(confirm('Delete this job post?')) {
      this.careerService.deleteJob(id).subscribe(() => {
        this.toastr.success('Job deleted');
        this.loadJobs();
        this.cdr.detectChanges();
      });
    }
  }

  viewJobDetails(job: any): void {
    this.ngZone.run(() => {
      this.selectedJobForDetails = job;
      this.cdr.detectChanges();
    });
  }

  applyForJob(job: any): void {
    if (!this.userId) {
      this.toastr.info('Please sign up to apply for this position');
      this.router.navigate(['/login'], { queryParams: { mode: 'signup', returnUrl: '/careers' } });
      return;
    }
    
    if (this.appliedJobs.has(job.id)) {
      this.toastr.warning('You have already applied for this position');
      return;
    }
    this.selectedJobForDetails = null; // Close details if open
    this.selectedJobForApplication = job;
  }

  onApplicationSuccess(): void {
    const jobId = this.selectedJobForApplication.id;
    this.appliedJobs.add(jobId);
    this.selectedJobForApplication = null;
    
    // Defer the refresh to next tick to avoid NG0100
    setTimeout(() => {
      this.loadMyApplications();
    }, 0);
  }

  viewApplicants(job: any): void {
    this.ngZone.run(() => {
      this.selectedJobForApplicants = job;
      this.applicantSearchQuery = ''; // Reset search when opening new pool
      this.careerService.getApplicants(job.id).subscribe(res => {
        this.applicants = res;
        this.cdr.detectChanges();
      });
    });
  }

  get filteredApplicants(): any[] {
    if (!this.applicantSearchQuery.trim()) return this.applicants;
    const query = this.applicantSearchQuery.toLowerCase();
    return this.applicants.filter(app => 
      app.employeeName?.toLowerCase().includes(query) || 
      app.employeeEmail?.toLowerCase().includes(query) ||
      app.status?.toLowerCase().includes(query)
    );
  }

  changeStatus(appId: string, status: string): void {
    this.careerService.updateStatus(appId, status).subscribe({
      next: () => {
        this.toastr.success(`Applicant marked as ${status}`);
        this.viewApplicants(this.selectedJobForApplicants);
      },
      error: () => this.toastr.error('Failed to update status')
    });
  }

  toggleAiInsight(appId: string): void {
    if (this.expandedAppId === appId) {
      this.expandedAppId = null;
    } else {
      this.expandedAppId = appId;
    }
  }
}