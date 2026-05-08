import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ShiftService, Shift, EmployeeShift } from '../../services/shift.service';
import { EmployeeService, Employee } from '../../services/employee';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-shift-assignment',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './shift-assignment.html',
  styleUrls: ['./shift-assignment.css']
})
export class ShiftAssignmentComponent implements OnInit {
  employees: Employee[] = [];
  shifts: Shift[] = [];
  assignments: any[] = [];
  
  selectedEmployeeIds: (string | number)[] = [];
  selectedAssignmentIds: (string | number)[] = [];
  viewMode: 'daily' | 'all' = 'all';
  viewDate = (() => {
    const d = new Date();
    const day = String(d.getDate()).padStart(2, '0');
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const year = String(d.getFullYear()).slice(-2);
    return `${day}-${month}-${year}`;
  })();
  
  empSearchTerm = '';
  assignmentSearchTerm = '';
  
  newAssignment: any = {
    shiftId: '',
    startDate: new Date().toISOString().split('T')[0],
    endDate: new Date().toISOString().split('T')[0]
  };

  constructor(
    private shiftService: ShiftService,
    private employeeService: EmployeeService,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadInitialData();
    console.log("Edited");
  }

  loadInitialData(): void {
    // Load employees first, then shifts, then assignments to ensure names are resolved correctly
    this.employeeService.getAllEmployees().subscribe({
      next: (emps) => {
        this.employees = emps.filter((e: any) => e.role === 'EMPLOYEE');
        this.shiftService.getShifts().subscribe({
          next: (shifts) => {
            this.shifts = shifts;
            this.loadAssignments();
            this.cdr.detectChanges();
          },
          error: () => this.toastr.error('Failed to load shifts')
        });
      },
      error: () => this.toastr.error('Failed to load employees')
    });
  }

  loadEmployees(): void {
    this.employeeService.getAllEmployees().subscribe({
      next: (data) => {
        this.employees = data.filter((e: any) => e.role === 'EMPLOYEE');
        this.cdr.detectChanges();
      },
      error: () => this.toastr.error('Failed to load employees')
    });
  }

  loadShifts(): void {
    this.shiftService.getShifts().subscribe({
      next: (data) => {
        this.shifts = data;
        this.cdr.detectChanges();
      },
      error: () => this.toastr.error('Failed to load shifts')
    });
  }

  loadAssignments(): void {
    const effectiveDate = this.formatDate(this.viewDate);
    const obs = this.viewMode === 'all' 
      ? this.shiftService.getAllAssignments() 
      : this.shiftService.getAssignmentsByDate(effectiveDate);

    obs.subscribe({
      next: (data) => {
        this.assignments = data.map(assignment => {
          const shift = this.shifts.find(s => s.id === assignment.shiftId);
          return {
            ...assignment,
            startDate: this.formatDate(assignment.startDate),
            endDate: this.formatDate(assignment.endDate),
            employeeName: this.employees.find(e => e.id === assignment.employeeId)?.name || 'Unknown',
            shiftName: shift?.shiftName || 'Unknown',
            shiftTimings: shift ? `${shift.startTime} - ${shift.endTime}` : 'N/A'
          };
        });
        this.cdr.detectChanges();
      },
      error: () => this.toastr.error('Failed to load assignments')
    });
  }

  onAssign(): void {
    if (this.selectedEmployeeIds.length === 0 || !this.newAssignment.shiftId) {
      this.toastr.warning('Please select at least one employee and a shift');
      return;
    }

    if (new Date(this.newAssignment.endDate) < new Date(this.newAssignment.startDate)) {
      this.toastr.error('End date cannot be before start date');
      return;
    }

    const bulkData = {
      employeeIds: this.selectedEmployeeIds,
      shiftId: this.newAssignment.shiftId,
      startDate: this.formatDate(this.newAssignment.startDate),
      endDate: this.formatDate(this.newAssignment.endDate)
    };

    this.shiftService.bulkAssign(bulkData).subscribe({
      next: () => {
        this.toastr.success(`Shifts assigned successfully for the selected period`);
        this.selectedEmployeeIds = [];
        this.loadAssignments();
        this.cdr.detectChanges();
      },
      error: () => this.toastr.error('Failed to assign shifts')
    });
  }

  deleteAssignment(id: string | number): void {
    if (confirm('Are you sure you want to remove this assignment?')) {
      this.shiftService.deleteAssignment(id).subscribe({
        next: () => {
          this.toastr.success('Assignment removed');
          this.loadAssignments();
        },
        error: () => this.toastr.error('Failed to remove assignment')
      });
    }
  }

  bulkDelete(): void {
    if (this.selectedAssignmentIds.length === 0) return;
    
    if (confirm(`Are you sure you want to delete ${this.selectedAssignmentIds.length} assignments?`)) {
      this.shiftService.bulkDeleteAssignments(this.selectedAssignmentIds).subscribe({
        next: () => {
          this.toastr.success('Assignments deleted successfully');
          this.selectedAssignmentIds = [];
          this.loadAssignments();
        },
        error: () => this.toastr.error('Failed to perform bulk delete')
      });
    }
  }

  toggleEmployee(empId: string | number): void {
    const index = this.selectedEmployeeIds.indexOf(empId);
    if (index > -1) {
      this.selectedEmployeeIds.splice(index, 1);
    } else {
      this.selectedEmployeeIds.push(empId);
    }
  }

  toggleAssignmentSelection(id: string | number): void {
    const index = this.selectedAssignmentIds.indexOf(id);
    if (index > -1) {
      this.selectedAssignmentIds.splice(index, 1);
    } else {
      this.selectedAssignmentIds.push(id);
    }
  }

  toggleSelectAllEmployees(event: any): void {
    if (event.target.checked) {
      this.selectedEmployeeIds = this.employees.map(e => e.id!);
    } else {
      this.selectedEmployeeIds = [];
    }
  }

  toggleSelectAllAssignments(event: any): void {
    if (event.target.checked) {
      this.selectedAssignmentIds = this.assignments.map(a => a.id!);
    } else {
      this.selectedAssignmentIds = [];
    }
  }

  get filteredEmployees() {
    if (!this.empSearchTerm.trim()) return this.employees;
    const term = this.empSearchTerm.toLowerCase();
    return this.employees.filter(e => 
      e.name.toLowerCase().includes(term) || 
      e.email.toLowerCase().includes(term)
    );
  }

  get filteredAssignments() {
    if (!this.assignmentSearchTerm.trim()) return this.assignments;
    const term = this.assignmentSearchTerm.toLowerCase();
    return this.assignments.filter(a => 
      a.employeeName.toLowerCase().includes(term) || 
      a.shiftName.toLowerCase().includes(term) ||
      a.shiftTimings.toLowerCase().includes(term)
    );
  }

  private formatDate(dateStr: string): string {
    if (!dateStr) return '';
    // If it's already dd-MM-yy (contains hyphen and first part length is 2)
    const parts = dateStr.split('-');
    if (parts.length === 3 && parts[0].length === 2) return dateStr;
    
    // If it's yyyy-MM-dd
    const [year, month, day] = parts;
    return `${day}-${month}-${year.slice(-2)}`;
  }
}
