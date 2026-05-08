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
    this.cdr.detectChanges();

    const { email, password } = this.loginForm.value;
    const role = undefined; // Role is not selected during login

    this.authService.login(email, password, role).subscribe({
      next: (response: any) => {
        this.loading = false;
        this.toastr.success('Login successful!');
        this.navigateDashboard(response.role);
      },
      error: (error: any) => {
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
      }
    });
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