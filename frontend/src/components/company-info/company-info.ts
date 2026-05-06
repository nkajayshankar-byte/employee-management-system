import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EmployeeService } from '../../services/employee';
import { AuthService } from '../../services/auth';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-company-info',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './company-info.html',
  styleUrls: ['./company-info.css']
})
export class CompanyInfoComponent implements OnInit {

  companyDetails: any = {
    name: '',
    founded: '',
    employees: 0,
    locations: [] as string[],
    mission: '',
    vision: '',
    values: [] as string[],
    perks: [] as any[],
    testimonials: [] as any[],
    faqs: [] as any[],
    cultureHighlights: [] as any[],
    contactInfo: {} as any
  };

  blogPosts: any[] = [];
  
  isEmployee: boolean = false;
  newTestimonial: string = '';
  apiUrl = environment.apiUrl;
  originalCompany: any = null;

  constructor(
    private service: EmployeeService,
    private auth: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.isEmployee = this.auth.hasRole('EMPLOYEE');
    this.loadCompany();
  }

  loadCompany() {
    this.service.getCompany().subscribe({
      next: (res: any) => {
        const company = res.company || {};
        this.originalCompany = company; // Store original entity for safe saving
        this.companyDetails = {
          ...company,
          employees: res.employeeCount || 0,
          locations: (res.locations || []).map((l: any) => l.city),
        };
        this.blogPosts = res.blogPosts || [];
        this.cdr.detectChanges(); 
      },
      error: () => {
        console.error('Failed to load company data');
        this.cdr.detectChanges();
      }
    });
  }

  submitTestimonial() {
    if (!this.newTestimonial.trim()) return;
    
    const user = this.auth.getCurrentUser();
    if (!user) return;

    if (!this.originalCompany.testimonials) {
      this.originalCompany.testimonials = [];
    }

    this.originalCompany.testimonials.push({
      name: user.name || 'Team Member',
      role: user.jobTitle || 'Employee', // Use hired job title
      quote: this.newTestimonial.trim(),
      imageUrl: user.imageUrl || '' // Use their profile image if available
    });

    // Save back the pure backend entity to avoid parsing issues
    this.service.saveCompany(this.originalCompany).subscribe({
      next: () => {
        alert('Thank you! Your testimonial has been shared.');
        this.newTestimonial = '';
        this.loadCompany();
      },
      error: (err) => {
        console.error('Save error:', err);
        alert('Failed to submit testimonial. Please try again.');
      }
    });
  }
}