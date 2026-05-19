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
  currentPage = 1;
  pageSize = 10;
  
  selectedAssetIds = new Set<string | number>();
  selectAll = false;

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
        this.assets = (data || []).map(a => this.parseAssetDates(a));
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
        this.assets = (data || []).map(a => this.parseAssetDates(a));
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
    this.currentPage = 1;
    this.selectedAssetIds.clear();
    this.selectAll = false;
    
    if (!this.searchTerm.trim()) {
      this.filteredAssets = [...this.assets];
      return;
    }

    this.assetService.searchAssets(this.searchTerm).subscribe({
      next: (data: any[]) => {
        this.filteredAssets = (data || []).map(a => this.parseAssetDates(a));
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

  get paginatedAssets(): any[] {
    const startIndex = (this.currentPage - 1) * this.pageSize;
    return this.filteredAssets.slice(startIndex, startIndex + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredAssets.length / this.pageSize);
  }

  prevPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.updateSelectAllState();
      this.cdr.detectChanges();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.updateSelectAllState();
      this.cdr.detectChanges();
    }
  }

  toggleSelection(id: string | number): void {
    if (this.selectedAssetIds.has(id)) {
      this.selectedAssetIds.delete(id);
    } else {
      this.selectedAssetIds.add(id);
    }
    this.updateSelectAllState();
  }

  toggleSelectAll(): void {
    this.selectAll = !this.selectAll;
    if (this.selectAll) {
      this.paginatedAssets.forEach(asset => this.selectedAssetIds.add(asset.id));
    } else {
      this.paginatedAssets.forEach(asset => this.selectedAssetIds.delete(asset.id));
    }
  }

  private updateSelectAllState(): void {
    if (this.paginatedAssets.length === 0) {
      this.selectAll = false;
      return;
    }
    this.selectAll = this.paginatedAssets.every(asset => this.selectedAssetIds.has(asset.id));
  }

  bulkDeleteSelected(): void {
    if (this.selectedAssetIds.size === 0) return;
    
    if (confirm(`Are you sure you want to delete ${this.selectedAssetIds.size} selected assets?`)) {
      this.loading = true;
      const idsToDelete = Array.from(this.selectedAssetIds);
      this.assetService.bulkDeleteAssets(idsToDelete).subscribe({
        next: () => {
          this.toastr.success(`${idsToDelete.length} assets deleted successfully`);
          this.selectedAssetIds.clear();
          this.selectAll = false;
          if (this.isAdmin) {
            this.loadAllAssets();
          } else {
            this.loadEmployeeAssets();
          }
        },
        error: () => {
          this.loading = false;
          this.toastr.error('Failed to delete selected assets');
          this.cdr.detectChanges();
        }
      });
    }
  }

  private parseAssetDates(asset: any): any {
    if (!asset) return asset;
    if (asset.assignedDate) asset.assignedDate = this.formatDateString(asset.assignedDate);
    if (asset.returnDate) asset.returnDate = this.formatDateString(asset.returnDate);
    return asset;
  }

  private formatDateString(dateStr: string): string {
    if (!dateStr) return '';
    const parts = dateStr.trim().split(' ');
    const dateParts = parts[0].split('-');
    
    if (dateParts.length === 3) {
      let yy = dateParts[2];
      let mm = dateParts[1];
      let dd = dateParts[0];
      
      // If year is first (yyyy-MM-dd)
      if (dateParts[0].length === 4) return dateStr;
      
      if (yy.length === 2) yy = '20' + yy;
      const timePart = parts[1] ? `T${parts[1]}:00` : 'T00:00:00';
      return `${yy}-${mm}-${dd}${timePart}`;
    }
    return dateStr;
  }
}