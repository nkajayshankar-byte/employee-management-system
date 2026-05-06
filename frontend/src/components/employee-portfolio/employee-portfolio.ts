import { Component, OnInit, HostListener, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { Employee, EmployeeService } from '../../services/employee';
import { environment } from '../../environments/environment';
import { AuthService } from '../../services/auth';
import { HasUnsavedChanges } from '../../services/unsaved-changes.guard';

@Component({
  selector: 'app-employee-portfolio',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './employee-portfolio.html',
  styleUrls: ['./employee-portfolio.css']
})
export class EmployeePortfolioComponent implements OnInit, HasUnsavedChanges {
  employee: Employee | null = null;
  pristineEmployee: string = '';
  loading = false;
  editMode = false;
  selectedFile: File | null = null;

  constructor(
    private employeeService: EmployeeService,
    private authService: AuthService,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef,
    private ngZone: NgZone
  ) { }

  ngOnInit(): void {
    this.loadPortfolio();
  }

  loadPortfolio(): void {
    setTimeout(() => {
      this.loading = true;
      this.cdr.detectChanges();
    }, 0);

    const currentUser = this.authService.getCurrentUser();

    if (!currentUser || !currentUser.email) {
      this.ngZone.run(() => {
        this.loading = false;
        this.cdr.detectChanges();
      });
      return;
    }

    this.employeeService.getEmployeeByEmail(currentUser.email).subscribe({
      next: (data) => {
        this.ngZone.run(() => {
          this.employee = data;
          this.pristineEmployee = JSON.stringify(data);
          this.loading = false;
          this.cdr.detectChanges();
        });
      },
      error: (err) => {
        this.ngZone.run(() => {
          this.toastr.error('Failed to load portfolio');
          this.loading = false;
          this.cdr.detectChanges();
        });
      }
    });
  }

  toggleEdit(): void {
    this.ngZone.run(() => {
      this.editMode = !this.editMode;
      this.cdr.detectChanges();
    });
  }

  onFileSelected(event: any): void {
    this.selectedFile = event.target.files[0];
  }

  uploadImage(): void {
    if (this.employee?.id && this.selectedFile) {
      this.employeeService.uploadImage(this.employee.id, this.selectedFile).subscribe({
        next: (imageUrl) => {
          this.ngZone.run(() => {
            if (this.employee) {
              this.employee.imageUrl = imageUrl;
            }
            this.toastr.success('Image uploaded successfully');
            this.selectedFile = null;
            this.cdr.detectChanges();
          });
        },
        error: () => {
          this.ngZone.run(() => {
            this.toastr.error('Failed to upload image');
            this.cdr.detectChanges();
          });
        }
      });
    }
  }

  saveChanges(): void {
    if (this.employee?.id) {
      this.ngZone.run(() => {
        this.loading = true;
        this.cdr.detectChanges();
      });

      this.employeeService.updateEmployee(this.employee.id, this.employee).subscribe({
        next: () => {
          this.ngZone.run(() => {
            this.toastr.success('Portfolio updated successfully');
            this.editMode = false;
            if (this.employee) {
              this.pristineEmployee = JSON.stringify(this.employee);
            }
            this.loadPortfolio();
            this.loading = false;
            this.cdr.detectChanges();
          });
        },
        error: (err) => {
          this.ngZone.run(() => {
            this.toastr.error('Failed to update portfolio');
            this.loading = false;
            this.cdr.detectChanges();
          });
        }
      });
    }
  }

  @HostListener('window:beforeunload', ['$event'])
  unloadNotification($event: any): void {
    if (this.hasUnsavedChanges()) {
      $event.returnValue = true;
    }
  }

  hasUnsavedChanges(): boolean {
    if (!this.employee || !this.editMode) return false;
    return this.pristineEmployee !== JSON.stringify(this.employee);
  }
}