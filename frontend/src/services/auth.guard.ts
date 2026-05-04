import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from './auth';


@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) { }

  canActivate(route: ActivatedRouteSnapshot): boolean {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return false;
    }

    const requiredRole = route.data['role'];
    const requiredRoles = route.data['roles'];

    if (requiredRole && !this.authService.hasRole(requiredRole)) {
      this.router.navigate(['/login']);
      return false;
    }

    if (requiredRoles && Array.isArray(requiredRoles)) {
      const user = this.authService.getCurrentUser();
      if (!user || !requiredRoles.includes(user.role)) {
        this.router.navigate(['/login']);
        return false;
      }
    } else if (requiredRoles && !this.authService.hasRole(requiredRoles)) {
      // support single string in 'roles' too
      this.router.navigate(['/login']);
      return false;
    }

    return true;
  }
}