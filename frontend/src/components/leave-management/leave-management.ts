import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

import { CommonModule } from '@angular/common';
import { LeaveService } from '../../services/leave-service';
import { ActivatedRoute, Router } from '@angular/router';
@Component({
  selector: 'app-leave-management',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './leave-management.html',
  styleUrls: ['./leave-management.css']
})
export class LeaveManagementComponent implements OnInit {
  leaves: any[] = [];
  filteredLeaves: any[] = [];
  loading = true;
  showApplyForm = false;
  showApprovalForm = false;
  selectedLeave: any = null;
  leaveForm!: FormGroup;
  approvalForm!: FormGroup;
  isAdmin = false;
  isEmployee = false;
  userId!: number;
  currentUserName: string = '';
  statistics: any = {};
  filterStatus = 'ALL';

  leaveTypes = ['Sick Leave', 'Casual Leave', 'Paid Leave', 'Unpaid Leave', 'Maternity Leave', 'Paternity Leave', 'Earned Leave'];
  statuses = ['PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'];

  constructor(
    private leaveService: LeaveService,
    private toastr: ToastrService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef,
    private route: ActivatedRoute,   
  private router: Router
  ) {
    this.createForms();
  }

  ngOnInit(): void {
    const user = JSON.parse(localStorage.getItem('currentUser') || '{}');
    this.isAdmin = user.role === 'ADMIN';
    this.isEmployee = user.role === 'EMPLOYEE';
    this.userId = user.userId;
    this.currentUserName = user.fullName || '';

     const id = this.route.snapshot.paramMap.get('id');

  if (id) {
    this.getLeaveById(id);  // ✅ LOAD SPECIFIC LEAVE
  }

    if (this.isAdmin) {
      this.loadAllLeaves();
      this.loadStatistics();
    } else if (this.isEmployee) {
      this.loadMyLeaves();
    }
  }

  createForms(): void {
    this.leaveForm = this.fb.group({
      leaveType: ['', Validators.required],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      reason: ['', Validators.required]
    });

    this.approvalForm = this.fb.group({
      approverName: ['', Validators.required],
      comments: ['']
    });
  }

  get totalWorkingDays(): number {
    const start = this.leaveForm.get('startDate')?.value;
    const end = this.leaveForm.get('endDate')?.value;
    if (!start || !end) return 0;
    return this.calculateWorkingDays(start, end);
  }

  // ✅ Employee - Apply for leave
  loadMyLeaves(): void {
  this.loading = true;

  this.leaveService.getMyLeaves().subscribe({
    next: (data: any[]) => {
      this.leaves = data || [];
      this.filteredLeaves = [...this.leaves]; // ✅ IMPORTANT

      this.loading = false;
      this.cdr.detectChanges();
    },
    error: () => {
      this.toastr.error('Failed to load leaves');
      this.loading = false;
      this.cdr.detectChanges();
    }
  });
}

  // ✅ Admin - Load all leaves
  loadAllLeaves(): void {
    this.loading = true;
    this.leaveService.getAllLeaves().subscribe({
      next: (data) => {
          this.leaves = (data || []).filter(item => item != null);
        this.filteredLeaves = [...this.leaves];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.toastr.error('Failed to load leaves');
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  loadStatistics(): void {
    this.leaveService.getLeaveStatistics().subscribe({
      next: (data) => {
        this.statistics = data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.toastr.error('Failed to load statistics');
      }
    });
  }

  openApplyForm(): void {
    this.showApplyForm = true;
    this.leaveForm.reset({
      employeeId: this.userId,
      employeeName: this.currentUserName,
      leaveType: '',
      startDate: '',
      endDate: '',
      reason: ''
    });
    this.cdr.detectChanges();
  }

  closeApplyForm(): void {
    this.showApplyForm = false;
    this.cdr.detectChanges();
  }

    applyLeave(): void {
      if (this.leaveForm.invalid) {
        this.toastr.error('Please fill all required fields');
        return;
      }

      const formatDate = (dateStr: string): string => {
        if (!dateStr) return '';
        const [year, month, day] = dateStr.split('-');
        return `${day}-${month}-${year.slice(-2)}`;
      };

      const userIdString = this.userId ? String(this.userId) : '';

      const payload = {
        ...this.leaveForm.value,
        startDate: formatDate(this.leaveForm.value.startDate),
        endDate: formatDate(this.leaveForm.value.endDate),
        employeeId: userIdString,
        employeeName: this.currentUserName,
        status: 'PENDING'
      };

      console.log('Final Leave Payload:', payload);

      this.leaveService.applyLeave(payload).subscribe({
        next: () => {
          this.toastr.success('Leave applied successfully');
          this.closeApplyForm();
          this.loadMyLeaves();
        },
        error: (error) => {
          this.toastr.error(error.error?.message || 'Failed to apply leave. Please check your inputs.');
        }
      });
    }
getLeaveById(id: string): void {
  this.leaveService.getLeaveById(id).subscribe({
    next: (data) => {
      this.selectedLeave = data;

      // Pre-fill approver name but don't auto-open the form
      if (this.isAdmin) {
        this.approvalForm.patchValue({
          approverName: this.currentUserName
        });
      }

      this.cdr.detectChanges();
    },
    error: () => {
      this.toastr.error('Failed to fetch leave details');
    }
  });
}
  cancelLeave(id: string): void {
    if (confirm('Are you sure you want to cancel this leave?')) {
      this.leaveService.cancelLeave(id).subscribe({
        next: () => {
          this.toastr.success('Leave cancelled successfully');
          this.loadMyLeaves();
        },
        error: () => {
          this.toastr.error('Failed to cancel leave');
        }
      });
    }
  }

  // ✅ Admin - Filter leaves by status
  filterByStatus(status: string): void {
    this.filterStatus = status;
    if (status === 'ALL') {
      this.filteredLeaves = [...this.leaves];
    } else {
      this.filteredLeaves = this.leaves.filter(l => l.status === status);
    }
    this.cdr.detectChanges();
  }

  // ✅ Admin - Approve/Reject leave
  viewLeave(leave: any): void {
      if (!leave) return; // Add this null check

      this.selectedLeave = leave;
      this.approvalForm.patchValue({
        approverName: this.currentUserName
      });
      this.cdr.detectChanges();
    }

  closeLeaveDetail(): void {
    this.selectedLeave = null;
    this.showApprovalForm = false;
    this.cdr.detectChanges();
  }

  openApprovalForm(): void {
    this.showApprovalForm = true;
    this.cdr.detectChanges();
  }

  approveLeave(): void {
    if (this.approvalForm.invalid) {
      this.toastr.error('Please enter approver name');
      return;
    }

    this.leaveService.approveLeave(this.selectedLeave.id, this.approvalForm.value).subscribe({
      next: () => {
        this.toastr.success('Leave approved successfully');
        this.closeLeaveDetail();
        this.loadAllLeaves();
        this.loadStatistics();
      },
      error: () => {
        this.toastr.error('Failed to approve leave');
      }
    });
  }

  rejectLeave(): void {
    if (this.approvalForm.invalid) {
      this.toastr.error('Please enter approver name');
      return;
    }

    this.leaveService.rejectLeave(this.selectedLeave.id, this.approvalForm.value).subscribe({
      next: () => {
        this.toastr.success('Leave rejected successfully');
        this.closeLeaveDetail();
        this.loadAllLeaves();
        this.loadStatistics();
      },
      error: () => {
        this.toastr.error('Failed to reject leave');
      }
    });
  }

  getStatusBadgeClass(status: string): string {
    const classes: any = {
      'PENDING': 'badge-warning',
      'APPROVED': 'badge-success',
      'REJECTED': 'badge-danger',
      'CANCELLED': 'badge-secondary'
    };
    return classes[status] || 'badge-secondary';
  }

  calculateWorkingDays(startDate: string, endDate: string): number {
    if (!startDate || !endDate) return 0;
    
    const [sYear, sMonth, sDay] = startDate.split('-').map(Number);
    const [eYear, eMonth, eDay] = endDate.split('-').map(Number);
    
    const start = new Date(sYear, sMonth - 1, sDay);
    const end = new Date(eYear, eMonth - 1, eDay);
    
    if (start > end) return 0;

    let count = 0;
    let cur = new Date(start);
    while (cur <= end) {
      const dayOfWeek = cur.getDay();
      if (dayOfWeek !== 0 && dayOfWeek !== 6) { // 0: Sunday, 6: Saturday
        count++;
      }
      cur.setDate(cur.getDate() + 1);
    }
    return count;
  }
}
