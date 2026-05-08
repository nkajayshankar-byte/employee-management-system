import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ShiftService, Shift } from '../../services/shift.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-shift-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './shift-management.html',
  styleUrls: ['./shift-management.css']
})
export class ShiftManagementComponent implements OnInit {
  shifts: Shift[] = [];
  newShift: Shift = {
    shiftName: '',
    startTime: '',
    endTime: '',
    description: ''
  };
  isEditing = false;
  editingId: string | number = '';

  constructor(
    private shiftService: ShiftService,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadShifts();
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

  onSubmit(): void {
    if (this.isEditing) {
      this.shiftService.updateShift(this.editingId, this.newShift).subscribe({
        next: () => {
          this.toastr.success('Shift updated successfully');
          this.resetForm();
          this.loadShifts();
        },
        error: () => this.toastr.error('Failed to update shift')
      });
    } else {
      this.shiftService.createShift(this.newShift).subscribe({
        next: () => {
          this.toastr.success('Shift created successfully');
          this.resetForm();
          this.loadShifts();
        },
        error: () => this.toastr.error('Failed to create shift')
      });
    }
  }

  editShift(shift: Shift): void {
    this.isEditing = true;
    this.editingId = shift.id!;
    this.newShift = { ...shift };
  }

  deleteShift(id: string | number): void {
    if (confirm('Are you sure you want to delete this shift?')) {
      this.shiftService.deleteShift(id).subscribe({
        next: () => {
          this.toastr.success('Shift deleted successfully');
          this.loadShifts();
        },
        error: () => this.toastr.error('Failed to delete shift')
      });
    }
  }

  resetForm(): void {
    this.isEditing = false;
    this.editingId = '';
    this.newShift = {
      shiftName: '',
      startTime: '',
      endTime: '',
      description: ''
    };
  }
}
