import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PayrollService, Payslip } from '../../services/payroll.service';
import { AuthService } from '../../services/auth';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-employee-payslips',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './employee-payslips.html',
  styleUrls: ['./employee-payslips.css']
})
export class EmployeePayslipsComponent implements OnInit {
  payslips: Payslip[] = [];
  loading = true;
  employeeId: number | null = null;

  constructor(
    private payrollService: PayrollService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    if (user && user.userId) {
      this.employeeId = Number(user.userId);
      this.loadPayslips();
    }
  }

  loadPayslips(): void {
    if (!this.employeeId) return;
    this.loading = true;
    this.payrollService.getEmployeePayslips(this.employeeId).subscribe({
      next: (data) => {
        this.payslips = data || [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  getMonthName(month: number): string {
    const names = ['', 'January', 'February', 'March', 'April', 'May', 'June',
                   'July', 'August', 'September', 'October', 'November', 'December'];
    return names[month] || '';
  }

  downloadPdf(url: string): void {
    if (!url) return;
    let finalUrl = url;
    if (url.startsWith('/api/files/download/payslips/')) {
      finalUrl = `${environment.apiUrl}/uploads/payslips/${url.substring('/api/files/download/payslips/'.length)}`;
    } else if (url.startsWith('/uploads')) {
      finalUrl = `${environment.apiUrl}${url}`;
    }
    window.open(finalUrl, '_blank');
  }
}
