import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { EmployeeService } from '../../services/employee';
import { CareerService } from '../../services/carrerservice';
import { AuthService } from '../../services/auth';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-landing-page',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './landing-page.html',
  styleUrls: ['./landing-page.css']
})
export class LandingPageComponent implements OnInit {
  company: any = null;
  jobs: any[] = [];
  loading = true;
  isLoggedIn = false;
  showDropdown = false;
  employeeCount = 0;
  openFaqIndex: number | null = null;
  testimonialIndex = 0;
  cultureIndex = 0;

  constructor(
    private employeeService: EmployeeService,
    private careerService: CareerService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.isLoggedIn = !!this.authService.getCurrentUser();
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    
    // Load Company Data
    this.employeeService.getCompany().subscribe({
      next: (res: any) => {
        this.company = res.company;
        this.cdr.detectChanges();
      }
    });

    // Load Employee Count (exclude USER role)
    this.employeeService.getAllEmployees().subscribe({
      next: (employees: any[]) => {
        this.employeeCount = employees.filter((e: any) => e.role === 'EMPLOYEE' || e.role === 'ADMIN').length;
        this.cdr.detectChanges();
      }
    });

    // Load Careers Data
    this.careerService.getJobs().subscribe({
      next: (res: any) => {
        this.jobs = res.filter((j: any) => j.isActive !== false).slice(0, 4);
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  toggleFaq(index: number): void {
    this.openFaqIndex = this.openFaqIndex === index ? null : index;
  }

  prevTestimonial(): void {
    const len = this.company?.testimonials?.length || 0;
    this.testimonialIndex = (this.testimonialIndex - 1 + len) % len;
  }

  nextTestimonial(): void {
    const len = this.company?.testimonials?.length || 0;
    this.testimonialIndex = (this.testimonialIndex + 1) % len;
  }

  prevCulture(): void {
    const len = this.company?.cultureHighlights?.length || 0;
    this.cultureIndex = (this.cultureIndex - 1 + len) % len;
  }

  nextCulture(): void {
    const len = this.company?.cultureHighlights?.length || 0;
    this.cultureIndex = (this.cultureIndex + 1) % len;
  }

  onApply(job: any): void {
    // Force a fresh signup flow from the landing page as requested
    this.logoutAndSignup();
  }

  toggleDropdown(): void {
    this.showDropdown = !this.showDropdown;
  }

  logoutAndSignup(): void {
    this.authService.logout();
    this.isLoggedIn = false;
    this.showDropdown = false;
    this.router.navigate(['/login'], { queryParams: { mode: 'signup' } });
  }

  logoutAndLogin(): void {
    this.authService.logout();
    this.isLoggedIn = false;
    this.showDropdown = false;
    this.router.navigate(['/login']);
  }

  scrollToSection(sectionId: string): void {
    const element = document.getElementById(sectionId);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }
}
