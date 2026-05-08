import { Component, OnInit, ChangeDetectorRef, NgZone } from '@angular/core';
import * as XLSX from 'xlsx';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AttendanceService, Attendance } from '../../services/attendance.service';
import { ShiftService, EmployeeShift } from '../../services/shift.service';
import { AuthService } from '../../services/auth';
import { EmployeeService } from '../../services/employee';
import { ToastrService, ToastrModule } from 'ngx-toastr';

@Component({
  selector: 'app-attendance-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, ToastrModule],
  templateUrl: './attendance-dashboard.html',
  styleUrls: ['./attendance-dashboard.css']
})
export class AttendanceDashboardComponent implements OnInit {
  user: any;
  isAdmin = false;
  attendanceRecords: any[] = [];
  todayShift: any = null;
  todayAttendance: Attendance | null = null;
  loading = false;
  
  filterDate = new Date().toISOString().split('T')[0];
  employees: any[] = [];

  constructor(
    private attendanceService: AttendanceService,
    private shiftService: ShiftService,
    private authService: AuthService,
    private employeeService: EmployeeService,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef,
    private zone: NgZone
  ) {}

  ngOnInit(): void {
    this.user = this.authService.getCurrentUser();
    if (this.user) {
      this.isAdmin = this.authService.hasRole('ADMIN');
      this.loadInitialData();
    }
  }

  loadInitialData(): void {
    if (this.isAdmin) {
      this.employeeService.getAllEmployees().subscribe({
        next: (data) => {
          this.employees = data;
          this.loadData();
        },
        error: () => this.toastr.error('Failed to load employees')
      });
    } else {
      this.loadData();
    }
  }

  loadData(): void {
    if (this.isAdmin) {
      this.loadAllAttendance();
    } else {
      this.loadUserAttendance();
      this.loadTodayShift();
    }
  }

  loadUserAttendance(): void {
    this.attendanceService.getEmployeeAttendance(this.user.userId).subscribe({
      next: (data) => {
        this.attendanceRecords = data.map(record => ({
          ...record,
          employeeName: this.user.name
        }));
        
        // Build today's date in dd-MM-yy format to match backend
        this.zone.run(() => {
          this.attendanceRecords = data.map(record => ({
            ...record,
            employeeName: this.user.name
          }));
          
          // Build today's date in dd-MM-yy format to match backend
          const now = new Date();
          const day = String(now.getDate()).padStart(2, '0');
          const month = String(now.getMonth() + 1).padStart(2, '0');
          const year = String(now.getFullYear()).slice(-2);
          const todayStr = `${day}-${month}-${year}`;
          
          console.log('Matching against todayStr:', todayStr);
          console.log('First record date:', data[0]?.date);

          this.todayAttendance = data.find(a => {
            if (!a) return false;
            
            const getStr = (val: any): string => {
              if (typeof val === 'string') return val;
              if (Array.isArray(val)) return `${String(val[2]).padStart(2, '0')}-${String(val[1]).padStart(2, '0')}-${String(val[0]).slice(-2)}`;
              if (val && typeof val === 'object') {
                const d = val.dayOfMonth || val.day || val[2];
                const m = val.monthValue || val.month || val[1];
                const y = val.year || val[0];
                if (d && m && y) return `${String(d).padStart(2, '0')}-${String(m).padStart(2, '0')}-${String(y).toString().slice(-2)}`;
              }
              return '';
            };

            const recordDate = getStr(a.date);
            const checkInDate = getStr(a.checkInTime).split(' ')[0];
            
            const match = recordDate === todayStr || checkInDate === todayStr;
            if (match) console.log('Found match for today:', a);
            return match;
          }) || null;
          
          this.cdr.detectChanges();
        });
      },
      error: () => this.toastr.error('Failed to load attendance')
    });
  }

  loadAllAttendance(): void {
    const formattedDate = this.formatDateForBackend(this.filterDate);
    this.attendanceService.getAttendanceByDate(formattedDate).subscribe({
      next: (attendance) => {
        this.shiftService.getAssignmentsByDate(formattedDate).subscribe({
          next: (assignments) => {
            // Identify present/late employees
            const presentIds = attendance.map(a => a.employeeId);
            
            // Generate records for all assigned employees
            const combinedRecords: any[] = attendance.map(a => ({
              ...a,
              employeeName: this.employees.find(e => e.id === a.employeeId)?.name || 'Unknown'
            }));

            // Find missing employees (Absent) - skip weekends
            const filterDateObj = new Date(this.filterDate);
            const dayOfWeek = filterDateObj.getDay();
            const isWeekend = dayOfWeek === 0 || dayOfWeek === 6;

            if (!isWeekend) {
              assignments.forEach(as => {
                if (!presentIds.includes(as.employeeId)) {
                  combinedRecords.push({
                    employeeId: as.employeeId,
                    employeeName: this.employees.find(e => e.id === as.employeeId)?.name || 'Unknown',
                    date: this.filterDate,
                    checkInTime: null,
                    checkOutTime: null,
                    status: 'Absent',
                    workingHours: 0
                  });
                }
              });
            }

            this.attendanceRecords = combinedRecords;
            this.cdr.detectChanges();
          },
          error: () => this.toastr.error('Failed to load shift assignments')
        });
      },
      error: () => this.toastr.error('Failed to load attendance')
    });
  }

  loadTodayShift(): void {
    const now = new Date();
    now.setHours(0, 0, 0, 0);
    
    this.shiftService.getEmployeeShifts(this.user.userId).subscribe({
      next: (shifts) => {
        const assignment = shifts.find(s => {
          const start = this.parseDate(s.startDate);
          const end = this.parseDate(s.endDate);
          return start && end && now >= start && now <= end;
        });

        if (assignment) {
          this.shiftService.getShifts().subscribe(allShifts => {
            this.todayShift = allShifts.find(s => s.id === assignment.shiftId);
            this.cdr.detectChanges();
          });
        }
      }
    });
  }

  checkIn(): void {
    if (this.todayAttendance) {
      this.toastr.info('Already checked in for today');
      return;
    }
    
    this.loading = true;

    this.attendanceService.checkIn(this.user.userId).subscribe({
      next: (res) => {
        this.zone.run(() => {
          this.toastr.success('Checked in successfully');
          this.loadUserAttendance();
          this.loading = false;
          this.cdr.detectChanges();
        });
      },
      error: (err) => {
        this.zone.run(() => {
          this.loading = false;
          this.cdr.detectChanges();
          let msg = 'Check-in failed';
          if (err.error) {
            if (typeof err.error === 'string') {
              msg = err.error;
            } else if (typeof err.error === 'object') {
              msg = err.error.error || err.error.message || JSON.stringify(err.error);
            }
          } else if (err.message) {
            msg = err.message;
          }
          this.toastr.error(msg, 'Error');
        });
      }
    });
  }

  checkOut(): void {
    if (!this.todayAttendance) {
      this.toastr.warning('Please check in first');
      return;
    }
    if (this.todayAttendance.checkOutTime) {
      this.toastr.info('Already checked out for today');
      return;
    }

    this.loading = true;

    this.attendanceService.checkOut(this.user.userId).subscribe({
      next: (res) => {
        this.zone.run(() => {
          this.toastr.success('Checked out successfully');
          this.loadUserAttendance();
          this.loading = false;
          this.cdr.detectChanges();
        });
      },
      error: (err) => {
        this.zone.run(() => {
          this.loading = false;
          this.cdr.detectChanges();
          let msg = 'Check-out failed';
          if (err.error) {
            if (typeof err.error === 'string') {
              msg = err.error;
            } else if (typeof err.error === 'object') {
              msg = err.error.error || err.error.message || JSON.stringify(err.error);
            }
          } else if (err.message) {
            msg = err.message;
          }
          this.toastr.error(msg, 'Error');
        });
      }
    });
  }

  exportToExcel(): void {
    if (!this.attendanceRecords || this.attendanceRecords.length === 0) {
      this.toastr.warning('No records to export');
      return;
    }

    // Format data for export — backend already sends dd-MM-yy dates and HH:mm times
    const exportData = this.attendanceRecords.map(record => ({
      'Employee Name': record.employeeName,
      'Employee ID': record.employeeId,
      'Date': record.date || '--',
      'Check In': record.checkInTime ? record.checkInTime.split(' ')[1] || record.checkInTime : '--',
      'Check Out': record.checkOutTime ? record.checkOutTime.split(' ')[1] || record.checkOutTime : '--',
      'Status': record.status,
      'Working Hours': record.workingHours ? record.workingHours.toFixed(2) + 'h' : '--'
    }));

    // Create workbook and worksheet
    const worksheet = XLSX.utils.json_to_sheet(exportData);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Attendance');

    // Generate file and download
    XLSX.writeFile(workbook, `Attendance_Report_${this.filterDate}.xlsx`);
    this.toastr.success('Exported to Excel successfully');
  }

  getStatusClass(status: string): string {
    switch (status.toLowerCase()) {
      case 'present': return 'status-present';
      case 'late': return 'status-late';
      case 'absent': return 'status-absent';
      default: return '';
    }
  }

  private formatDateForBackend(dateStr: string): string {
    if (!dateStr) return '';
    const parts = dateStr.split('-');
    if (parts.length === 3 && parts[0].length === 2) return dateStr; // Already dd-MM-yy
    
    // Convert YYYY-MM-DD to dd-MM-yy
    const [year, month, day] = parts;
    return `${day}-${month}-${year.slice(-2)}`;
  }

  private parseDate(dateStr: string): Date | null {
    if (!dateStr) return null;
    const parts = dateStr.split('-');
    if (parts.length !== 3) return null;
    
    let day, month, year;
    if (parts[0].length === 4) { // yyyy-MM-dd
      [year, month, day] = parts.map(Number);
    } else { // dd-MM-yy
      [day, month, year] = parts.map(Number);
      if (year < 100) year += 2000;
    }
    return new Date(year, month - 1, day);
  }
}
