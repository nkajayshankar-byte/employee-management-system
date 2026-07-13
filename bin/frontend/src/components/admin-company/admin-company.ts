import { Component, OnInit, ChangeDetectorRef, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { EmployeeService } from '../../services/employee';
import { AuthService } from '../../services/auth';
import { HasUnsavedChanges } from '../../services/unsaved-changes.guard';

@Component({
  selector: 'app-admin-company',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-company.html',
  styleUrls: ['./admin-company.css']
})
export class AdminCompanyComponent implements OnInit, HasUnsavedChanges {

  company: any = {
    name: '',
    foundedYear: '',
    mission: '',
    vision: '',
    values: '',
    contactInfo: {
      email: '',
      phone: '',
      linkedin: '',
      twitter: '',
      instagram: ''
    },
    perks: [],
    testimonials: [],
    faqs: [],
    cultureHighlights: []
  };

  locations: any[] = [];
  locationInput: string = '';

  employeeCount = 0;
  locationCount = 0;

  expandedSections: any = {
    identity: true,
    narrative: true,
    culture: true,
    social: true,
    perks: false,
    testimonials: false,
    highlights: false,
    faqs: false
  };

  private initialState: string = '';

  constructor(
    private service: EmployeeService,
    private cdr: ChangeDetectorRef,
    private router: Router,
    private authService: AuthService
  ) {}

  @HostListener('window:beforeunload', ['$event'])
  unloadNotification($event: any) {
    if (this.hasUnsavedChanges()) {
      $event.returnValue = true;
    }
  }

  hasUnsavedChanges(): boolean {
    const currentState = JSON.stringify({ 
      company: this.company, 
      locations: this.locationInput 
    });
    return currentState !== this.initialState;
  }

  ngOnInit(): void {
    this.loadCompany();
  }

  loadCompany() {
    this.service.getCompany().subscribe((res: any) => {
      const data = res.company || {};
      
      // Ensure nested structures exist
      if (!data.contactInfo) data.contactInfo = { email: '', phone: '', linkedin: '', twitter: '', instagram: '' };
      if (!data.perks) data.perks = [];
      if (!data.testimonials) data.testimonials = [];
      if (!data.faqs) data.faqs = [];
      if (!data.cultureHighlights) data.cultureHighlights = [];

      this.company = data;

      if (Array.isArray(this.company.values)) {
        this.company.values = this.company.values.join(', ');
      }

      this.locations = res.locations || [];
      this.locationInput = this.locations
        .map((l: any) => l.city)
        .join(', ');

      this.employeeCount = res.employeeCount;
      this.locationCount = res.locationCount;

      // Capture initial state for change detection
      this.initialState = JSON.stringify({ 
        company: this.company, 
        locations: this.locationInput 
      });

      this.cdr.detectChanges(); 
    });
  }

  saveCompany() {
    const dataToSave = { ...this.company };

    if (typeof dataToSave.values === 'string') {
      dataToSave.values = dataToSave.values
        .split(',')
        .map((v: string) => v.trim())
        .filter((v: string) => v);
    }

    const locationArray = this.locationInput
      .split(',')
      .map((l: string) => l.trim())
      .filter((l: string) => l)
      .map((city: string) => ({ city }));

    this.service.saveCompany(dataToSave).subscribe({
      next: () => {
        this.service.replaceLocations(locationArray).subscribe({
          next: () => {
            alert('Company updated successfully');
            this.loadCompany(); // This will also reset initialState
          },
          error: (err) => {
            console.error(err);
            alert('Error updating locations');
          }
        });
      },
      error: (err) => {
        console.error(err);
        alert('Error saving company');
      }
    });
  }

  // Helper methods for dynamic lists
  addPerk(count: number = 1) {
    for (let i = 0; i < count; i++) {
      this.company.perks.push({ icon: 'fa-star', title: '', description: '' });
    }
  }

  addTestimonial(count: number = 1) {
    for (let i = 0; i < count; i++) {
      this.company.testimonials.push({ name: '', role: '', quote: '', imageUrl: '' });
    }
  }

  addFAQ(count: number = 1) {
    for (let i = 0; i < count; i++) {
      this.company.faqs.push({ question: '', answer: '' });
    }
  }

  addCultureHighlight(count: number = 1) {
    for (let i = 0; i < count; i++) {
      this.company.cultureHighlights.push({ title: '', description: '', imageUrl: '' });
    }
  }

  toggleSection(section: string) {
    this.expandedSections[section] = !this.expandedSections[section];
  }

  removeItem(list: any[], index: number) {
    list.splice(index, 1);
  }

  onImageUpload(event: any, item: any) {
    const file = event.target.files[0];
    if (file) {
      this.service.uploadCompanyImage(file).subscribe({
        next: (res) => {
          item.imageUrl = res.url;
          alert('Image uploaded successfully!');
        },
        error: (err) => {
          console.error('Error uploading image', err);
          alert('Failed to upload image.');
        }
      });
    }
  }

  getLocationCount(): string {
    if (!this.locationInput) return '0';
    return this.locationInput
      .split(',')
      .map(l => l.trim())
      .filter(l => l)
      .length.toString();
  }

  logoutAndLogin() {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}