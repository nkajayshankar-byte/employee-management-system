import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../../services/auth';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './reset-password-component.html',
  styleUrls: ['./reset-password-component.css']
})
export class ResetPasswordComponent {
  step = 1; // 1: Email, 2: OTP, 3: New Password
  emailForm: FormGroup;
  otpForm: FormGroup;
  passwordForm: FormGroup;
  loading = false;
  email = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef
  ) {
    this.emailForm = this.fb.group({
      email: ['', [Validators.required, Validators.pattern(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.com$/)]]
    });

    this.otpForm = this.fb.group({
      otp: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]]
    });

    this.passwordForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, {
      validators: (control: any) => {
        return control.get('newPassword')?.value === control.get('confirmPassword')?.value
          ? null
          : { passwordMismatch: true };
      }
    });
  }

  isInvalid(form: FormGroup, controlName: string): boolean {
    const control = form.get(controlName);
    return !!(control && control.invalid && (control.touched || control.dirty));
  }

  sendOtp(): void {
    if (this.emailForm.invalid) {
      this.emailForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.email = this.emailForm.value.email;

    this.authService.sendOtp(this.email).pipe(
      finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: () => {
        this.toastr.success('OTP sent to your email');
        this.step = 2;
      },
      error: (err) => {
        this.toastr.error(err.error?.message || 'Failed to send OTP. Check your email address.');
      }
    });
  }

  verifyOtp(): void {
    if (this.otpForm.invalid) {
      this.otpForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    const otp = this.otpForm.value.otp;

    this.authService.verifyOtp(this.email, otp).pipe(
      finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: () => {
        this.toastr.success('OTP verified successfully');
        this.step = 3;
      },
      error: (err) => {
        this.toastr.error(err.error?.message || 'Invalid OTP');
      }
    });
  }

  resetPassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      this.toastr.error('Please fill the form correctly');
      return;
    }

    this.loading = true;
    const { newPassword } = this.passwordForm.value;

    this.authService.resetPassword({ email: this.email, newPassword }).pipe(
      finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: () => {
        this.toastr.success('Password reset successfully!');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.toastr.error(err.error?.message || 'Failed to reset password');
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/login']);
  }
}