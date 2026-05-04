import { HttpClient } from '@angular/common/http';
  import { Injectable } from '@angular/core';
  import { Observable, BehaviorSubject } from 'rxjs';
  import { tap } from 'rxjs/operators';

  export interface AuthResponse {
    token: string;
    userId: string;
    email: string;
    name: string;
    role: string;
    companyInfo?: string; // kept for backward compat
    jobTitle?: string; // Hired job title
    message: string;
    imageUrl?: string;
    skills?: string;
    address?: string;
  }

  export interface AuthRequest {
    email: string;
    password: string;
    role?: string;
  }

  @Injectable({
    providedIn: 'root'
  })
  export class AuthService {
    private apiUrl = 'http://localhost:8080/api/auth';

    private currentUserSubject = new BehaviorSubject<AuthResponse | null>(
      this.getCurrentUser()
    );
    public currentUser$ = this.currentUserSubject.asObservable();

    constructor(private http: HttpClient) {}

    signup(user: AuthRequest): Observable<AuthResponse> {
      return this.http.post<AuthResponse>(`${this.apiUrl}/signup`, user, {
        headers: { 'Content-Type': 'application/json' }
      });
    }

    login(email: string, password: string, role?: string):
  Observable<AuthResponse> {
      return this.http.post<AuthResponse>(`${this.apiUrl}/login`, {
        email, password
      }).pipe(
        tap(response => {
          if (response && response.token) {
            localStorage.setItem('token', response.token);
            localStorage.setItem('currentUser', JSON.stringify(response));
            this.currentUserSubject.next(response);
          }
        })
      );
    }

    logout(): void {
      localStorage.removeItem('token');
      localStorage.removeItem('currentUser');
      this.currentUserSubject.next(null);
    }

    getToken(): string | null {
      return localStorage.getItem('token');
    }

    getCurrentUser(): AuthResponse | null {
      const token = this.getToken();
      if (!token) return null;
      
      const user = localStorage.getItem('currentUser');
      try {
        return user ? JSON.parse(user) : null;
      } catch (e) {
        return null;
      }
    }

    isAuthenticated(): boolean {
      return !!this.getToken();
    }

    hasRole(role: string): boolean {
      const user = this.getCurrentUser();
      return user?.role === role;
    }

    sendOtp(email: string): Observable<any> {
      console.log('Sending OTP to:', email);
      return this.http.post(`${this.apiUrl}/send-otp`, { email }, { responseType: 'text' as 'json' });
    }

    verifyOtp(email: string, otp: string): Observable<any> {
      console.log('Verifying OTP for:', email);
      return this.http.post(`${this.apiUrl}/verify-otp`, { email, otp }, { responseType: 'text' as 'json' });
    }

    resetPassword(data: any): Observable<any> {
      console.log('Resetting password');
      return this.http.post(`${this.apiUrl}/reset-password`, data, { responseType: 'text' as 'json' });
    }
  }