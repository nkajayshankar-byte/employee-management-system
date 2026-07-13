import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';

import { Observable, combineLatest, filter } from 'rxjs';
import { SidebarComponent } from '../components/sidebar/sidebar';
import { AuthService } from '../services/auth';
import { map, startWith } from 'rxjs/operators';
import { Router, NavigationEnd } from '@angular/router';

import { ChatbotComponent } from '../components/chatbot/chatbot';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, SidebarComponent, ChatbotComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class AppComponent {
  title = 'Employee Management Portal';
  isLoggedIn$: Observable<boolean>;
  isAdmin$: Observable<boolean>;

  constructor(private authService: AuthService, private router: Router) {
    const loginState$ = this.authService.currentUser$.pipe(map(user => !!user));
    
    const urlState$ = this.router.events.pipe(
      filter(event => event instanceof NavigationEnd),
      map(event => (event as NavigationEnd).urlAfterRedirects),
      startWith(this.router.url),
      map(url => {
        // Hide sidebar on landing page, login page, and reset password page
        const publicRoutes = ['/', '/login', '/reset-password'];
        return !publicRoutes.includes(url.split('?')[0]);
      })
    );

    this.isLoggedIn$ = combineLatest([loginState$, urlState$]).pipe(
      map(([isLoggedIn, isInternalRoute]) => isLoggedIn && isInternalRoute)
    );

    this.isAdmin$ = this.authService.currentUser$.pipe(map(user => user?.role === 'ADMIN'));
  }
}