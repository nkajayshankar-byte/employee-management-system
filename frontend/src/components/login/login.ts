import { Component, ChangeDetectorRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators, ValidatorFn } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent implements OnInit {

  signupForm: FormGroup;
  loginForm: FormGroup;
  isSignup = false;
  loading = false;
  is2faPending = false;
  target2faNumber = '';
  pendingEmail = '';
  private pollingInterval: any;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef
  ) {
    // Signup Form
    this.signupForm = this.fb.group({
      email: ['', [Validators.required,
      Validators.pattern(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.com$/)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      address: [''],
      role: ['USER', Validators.required],
      adminKey: [''],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });

    // Login Form
    this.loginForm = this.fb.group({
      email: ['', [Validators.required,
      Validators.pattern(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.com$/)]],
      password: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    // Check if we should show signup mode initially
    this.route.queryParams.subscribe(params => {
      if (params['mode'] === 'signup') {
        this.isSignup = true;
        this.cdr.detectChanges();
      }
    });
  }

  ngOnDestroy(): void {
    this.clearPolling();
  }

  clearPolling(): void {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval);
      this.pollingInterval = null;
    }
  }

  // Custom validator to check if passwords match
  passwordMatchValidator: ValidatorFn = (control: any): {
    [key: string]:
    any
  } | null => {

    const group = control as FormGroup;
    const password = group.get('password')?.value;
    const confirm = group.get('confirmPassword')?.value;

    return password === confirm ? null : { passwordMismatch: true };
  };

  // Helper for HTML templates to show error messages
  isInvalid(form: FormGroup, controlName: string): boolean {
    const control = form.get(controlName);
    return !!(control && control.invalid && (control.touched ||
      control.dirty));
  }

  toggleMode(): void {
    this.isSignup = !this.isSignup;
    this.cdr.detectChanges();
  }

  onSignup(): void {
    if (this.signupForm.invalid) {
      this.signupForm.markAllAsTouched();
      this.toastr.error('Please fix the errors in the form');
      return;
    }

    const { email, password, role, adminKey, address } = this.signupForm.value;
    const user = { email, password, role, adminKey, address };

    this.loading = true;
    this.authService.signup(user).subscribe({
      next: () => {
        setTimeout(() => {
          this.loading = false;
          this.toastr.success('Account created successfully! Please login.');
          this.isSignup = false;
          this.loginForm.patchValue({ email });
          this.cdr.detectChanges();
        }, 0);
      },
      error: (err: any) => {
        setTimeout(() => {
          this.loading = false;
          let msg = 'Signup failed';
          if (err.status === 409 || err.error === 'Email already exists') msg = 'Email already exists';
          this.toastr.error(msg);
          this.cdr.detectChanges();
        }, 0);
      }
    });
  }
  onLogin(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      this.toastr.error('Please fix the errors in the form');
      return;
    }

    this.loading = true;

    const { email, password } = this.loginForm.value;
    const role = undefined; // Role is not selected during login

    this.authService.login(email, password, role).subscribe({
      next: (response: any) => {
        setTimeout(() => {
          this.loading = false;
          
          if (response.requires2fa) {
             this.is2faPending = true;
             this.target2faNumber = ('0' + response.targetNumber).slice(-2);
             this.pendingEmail = response.email;
             this.toastr.info('Please check your email for the 2FA code.');
             this.startPolling2fa();
             this.cdr.detectChanges();
          } else {
             this.toastr.success('Login successful!');
             this.navigateDashboard(response.role);
             this.cdr.detectChanges();
          }
        }, 0);
      },
      error: (error: any) => {
        setTimeout(() => {
          this.loading = false;
          let msg = "Login failed";
          
          // Use backend-provided message if available
          if (error.error && error.error.message) {
            msg = error.error.message;
          } else if (error.status === 401) {
            msg = "Invalid email or password";
          } else if (error.status === 403) {
            msg = "Wrong role selected";
          } else if (typeof error.error === 'string') {
            msg = error.error;
          }
          
          this.toastr.error(msg);
          this.cdr.detectChanges();
        }, 0);
      }
    });
  }

  startPolling2fa(): void {
    this.pollingInterval = setInterval(() => {
      this.authService.check2faStatus(this.pendingEmail).subscribe({
        next: (res: any) => {
          if (res.token) {
            this.clearPolling();
            
            // Set session properly
            if (typeof this.authService.setLoggedIn === 'function') {
               this.authService.setLoggedIn(res);
            } else {
               localStorage.setItem('token', res.token);
               localStorage.setItem('currentUser', JSON.stringify(res));
            }

            this.toastr.success('2FA Verified! Login successful!');
            this.navigateDashboard(res.role);
          } else if (res.status === 'FAILED') {
            this.clearPolling();
            this.is2faPending = false;
            this.toastr.error('2FA request was rejected or expired.');
            this.cdr.detectChanges();
          }
        },
        error: () => {
          this.clearPolling();
          this.is2faPending = false;
          this.toastr.error('Network error during 2FA.');
          this.cdr.detectChanges();
        }
      });
    }, 2000);
  }

  private navigateDashboard(role: string): void {
    if (role === 'ADMIN') {
      this.router.navigate(['/admin']);
    } else {
      this.router.navigate(['/employee/portfolio']);
    }
  }

  goToResetPassword(): void {
    this.router.navigate(['/reset-password']);
  }
}