import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AssetService } from '../../services/asset';

@Component({
  selector: 'app-asset-details',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './asset-details.html',
  styleUrls: ['./asset-details.css']
})
export class AssetDetailsComponent implements OnInit {
  asset: any = null;
  loading = true;
  isAdmin = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private assetService: AssetService,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const user = JSON.parse(localStorage.getItem('currentUser') || '{}');
    this.isAdmin = user.role === 'ADMIN';

    const id = this.route.snapshot.params['id'];
    if (id) {
      this.loadAsset(id);
    }
  }

  loadAsset(id: string): void {
    this.loading = true;
    this.assetService.getAssetById(id).subscribe({
      next: (asset: any) => {
        this.asset = asset;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.toastr.error('Failed to load asset');
        this.router.navigate([this.isAdmin ? '/admin/assets' : '/employee/assets']);
      }
    });
  }

  deleteAsset(): void {
    if (!this.asset || !this.isAdmin) return;

    if (confirm('Are you sure you want to decommission this asset?')) {
      this.assetService.deleteAsset(this.asset.id).subscribe({
        next: () => {
          this.toastr.success('Asset decommissioned successfully');
          this.router.navigate(['/admin/assets']);
        },
        error: () => this.toastr.error('Failed to delete asset')
      });
    }
  }

  goBack(): void {
    this.router.navigate([this.isAdmin ? '/admin/assets' : '/employee/assets']);
  }
}
