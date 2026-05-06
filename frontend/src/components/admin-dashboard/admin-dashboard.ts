 import { Component, OnInit, ChangeDetectorRef, NgZone } from '@angular/core';
  import { CommonModule } from '@angular/common';
  import { RouterModule } from '@angular/router';
  import { ToastrService } from 'ngx-toastr';
  import { Employee, EmployeeService } from '../../services/employee';
  import { AssetService } from '../../services/asset';
import { LeaveService } from '../../services/leave-service';
import { CareerService } from '../../services/carrerservice';
import { environment } from '../../environments/environment';

  @Component({
    selector: 'app-admin-dashboard',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './admin-dashboard.html',
    styleUrls: ['./admin-dashboard.css']
  })
  export class AdminDashboardComponent implements OnInit {

    employees: Employee[] = [];
    recentEmployees: Employee[] = [];
    pendingLeaves: any[] = [];
    pendingApplicants: any[] = [];
  apiUrl = environment.apiUrl;
    loading: boolean = true;
    today: Date = new Date();

    stats = {
      totalUsers: 0,
      totalEmployees: 0,
      totalAdmins: 0,
      pendingLeaves: 0,
      pendingApplicants: 0, 
      totalAssets: 0
    }

    constructor(
      private employeeService: EmployeeService,
      private leaveService: LeaveService,
      private assetService: AssetService,
       private careerService: CareerService,
      private toastr: ToastrService,
      private cdr: ChangeDetectorRef,
      private ngZone: NgZone
    ) {}

    ngOnInit(): void {
      this.loadDashboardData();
    }

   loadDashboardData(): void {
      this.loading = true;

      // Load all data in parallel
      Promise.all([
        this.employeeService.getAllEmployees().toPromise(),
        this.leaveService.getPendingLeaves().toPromise(),
        this.assetService.getAllAssets().toPromise(),
        this.careerService.getJobs().toPromise() 
      ]).then(async ([employees, leaves, assets, jobs]) => {
        this.employees = employees || [];
        this.recentEmployees = [...this.employees].reverse().slice(0, 5);
        this.pendingLeaves = leaves || [];

        // Fetch applicants for active jobs and filter for PENDING
        let allApplicants: any[] = [];
        if (jobs && Array.isArray(jobs)) {
          const activeJobs = jobs.filter((job: any) => job.isActive !== false);
          const applicantPromises = activeJobs.map((job: any) =>
            this.careerService.getApplicants(job.id).toPromise().then(apps => {
              // Attach job title to each applicant for dashboard display
              return (apps || []).map(app => ({ ...app, jobTitle: job.title }));
            })
          );
          const results = await Promise.all(applicantPromises);
          allApplicants = results.flat();
        }

        this.ngZone.run(() => {
          this.pendingApplicants = allApplicants.filter(app => app.status === 'PENDING').slice(0, 5);
          this.updateStats(assets || [], this.pendingApplicants.length);
          this.loading = false;
          this.cdr.detectChanges();
        });
      }).catch(err => {
        this.ngZone.run(() => {
          this.loading = false;
          this.toastr.error('Failed to load dashboard data');
          this.cdr.detectChanges();
        });
      });
    }

   updateStats(assets: any[], appCount: number): void {
    this.stats.totalUsers = this.employees?.length || 0;
    this.stats.totalEmployees = this.employees?.filter(e =>
  e.role?.toUpperCase() === 'EMPLOYEE').length || 0;
    this.stats.totalAdmins = this.employees?.filter(e =>
  e.role?.toUpperCase() === 'ADMIN').length || 0;
    this.stats.pendingLeaves = this.pendingLeaves?.length || 0;
    this.stats.totalAssets = assets?.length || 0;
    this.stats.pendingApplicants = appCount;
  }
  }