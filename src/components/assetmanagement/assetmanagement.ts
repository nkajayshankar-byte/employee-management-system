import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AssetService } from '../../services/asset';
import { EmployeeService } from '../../services/employee';
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-assetmanagement',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule
  ],
  templateUrl: './assetmanagement.html',
  styleUrls: ['./assetmanagement.css'],
})
export class AssetManagementComponent implements OnInit {

  assets: any[] = [];
  filteredAssets: any[] = [];
  loading = true;
  searchTerm = '';

  isAdmin = false;
  isEmployee = false;
  userId: string | null = null;

  constructor(
    private assetService: AssetService,
    private employeeService: EmployeeService,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    const user = JSON.parse(localStorage.getItem('currentUser') || '{}');

    this.isAdmin = user.role === 'ADMIN';
    this.isEmployee = user.role === 'EMPLOYEE';
    this.userId = user.userId || user.id || null;

    if (this.isAdmin) {
      this.loadAllAssets();
    } else if (this.isEmployee) {
      this.loadEmployeeAssets();
    }
  }

  loadAllAssets(): void {
    this.loading = true;
    this.assetService.getAllAssets().subscribe({
      next: (data: any[]) => {
        this.assets = data || [];
        this.filteredAssets = [...this.assets];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.toastr.error('Failed to load assets');
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  loadEmployeeAssets(): void {
    if (!this.userId) {
      this.toastr.error('User not found');
      return;
    }

    this.loading = true;
    this.assetService.getAssetsByEmployee(this.userId).subscribe({
      next: (data: any[]) => {
        this.assets = data || [];
        this.filteredAssets = [...this.assets];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.toastr.error('Failed to load assets');
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  searchAssets(): void {
    if (!this.searchTerm.trim()) {
      this.filteredAssets = [...this.assets];
      return;
    }

    this.assetService.searchAssets(this.searchTerm).subscribe({
      next: (data: any[]) => {
        this.filteredAssets = data || [];
        if (this.isEmployee && this.userId) {
          this.filteredAssets = this.filteredAssets.filter(
            asset => asset.employeeId === this.userId
          );
        }
        this.cdr.detectChanges();
      },
      error: () => {
        this.toastr.error('Search failed');
      }
    });
  }

  exportToExcel(): void {
    if (!this.isAdmin) return;

    const dataToExport = this.filteredAssets.map(asset => ({
      'Asset Name': asset.assetName,
      'Asset Type': asset.assetType,
      'Serial Number': asset.serialNumber,
      'Employee': asset.employeeName || 'N/A',
      'Status': asset.status,
      'Condition': asset.conditions,
      'Allocated Date': asset.assignedDate ? new Date(asset.assignedDate).toLocaleDateString() : 'N/A',
      'Return Date': asset.returnDate ? new Date(asset.returnDate).toLocaleDateString() : 'Not Returned'
    }));

    const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(dataToExport);
    const workbook: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Assets');
    XLSX.writeFile(workbook, 'Asset_Management_Report.xlsx');
    this.toastr.success('Asset data exported to Excel successfully');
  }
}