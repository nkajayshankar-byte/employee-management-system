import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AssetService } from '../../services/asset';

@Component({
  selector: 'app-edit-asset',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './edit-asset.html',
  styleUrls: ['./edit-asset.css']
})
export class EditAssetComponent implements OnInit {
  assetForm: FormGroup;
  loading = true;
  submitting = false;
  assetId: string | null = null;
  currentAsset: any = null;

  assetTypes = ['Laptop', 'Monitor', 'Keyboard', 'Mouse', 'Desk Phone', 'Mobile', 'Headphones', 'Other'];
  statuses = ['ALLOCATED', 'RETURNED', 'DAMAGED', 'LOST'];
  conditions = ['Good', 'Fair', 'Poor', 'Damaged'];

  constructor(
    private fb: FormBuilder,
    private assetService: AssetService,
    private route: ActivatedRoute,
    private router: Router,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef
  ) {
    this.assetForm = this.fb.group({
      employeeId: ['', Validators.required],
      employeeName: ['', Validators.required],
      assetName: ['', Validators.required],
      assetType: ['', Validators.required],
      serialNumber: ['', Validators.required],
      status: ['', Validators.required],
      assignedDate: ['', Validators.required],
      returnDate: [''],
      conditions: ['', Validators.required],
      description: ['']
    });
  }

  ngOnInit(): void {
    this.assetId = this.route.snapshot.params['id'];
    if (this.assetId) {
      this.loadAsset(this.assetId);
    }
  }

  loadAsset(id: string): void {
    this.loading = true;
    this.assetService.getAllAssets().subscribe({
      next: (assets: any[]) => {
        const asset = assets.find(a => a.id === id);
        if (asset) {
          this.currentAsset = asset;
          this.assetForm.patchValue({
            ...asset,
            assignedDate: asset.assignedDate?.split('T')[0] || '',
            returnDate: asset.returnDate?.split('T')[0] || '',
            description: asset.remarks || asset.description || ''
          });
        }
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.toastr.error('Failed to load asset');
        this.router.navigate(['/admin/assets']);
      }
    });
  }

  onSubmit(): void {
    if (this.assetForm.invalid || !this.assetId) {
      this.toastr.error('Please fix errors in the form');
      return;
    }

    this.submitting = true;
    const f = this.assetForm.value;

    const formatDate = (dateStr: string): string => {
      if (!dateStr) return '';
      if (dateStr.includes(' ')) return dateStr; // Already formatted or has time
      const [year, month, day] = dateStr.split('-');
      return `${day}-${month}-${year.slice(-2)} 00:00`;
    };

    const payload = {
      ...f,
      employeeId: String(f.employeeId),
      assignedDate: formatDate(f.assignedDate),
      returnDate: f.returnDate ? formatDate(f.returnDate) : null,
      remarks: f.description
    };

    this.assetService.updateAsset(this.assetId, payload).subscribe({
      next: () => {
        this.toastr.success('Asset updated successfully');
        this.router.navigate(['/admin/assets/view', this.assetId]);
      },
      error: () => {
        this.submitting = false;
        this.toastr.error('Failed to update asset');
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/admin/assets/view', this.assetId]);
  }
}
