import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth';

export const AuthGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/login']);
  }

  const requiredRole = route.data['role'];
  const requiredRoles = route.data['roles'];

  if (requiredRole && !authService.hasRole(requiredRole)) {
    return router.createUrlTree(['/login']);
  }

  if (requiredRoles && Array.isArray(requiredRoles)) {
    const user = authService.getCurrentUser();
    if (!user || !requiredRoles.includes(user.role)) {
      return router.createUrlTree(['/login']);
    }
  } else if (requiredRoles && !authService.hasRole(requiredRoles)) {
    // support single string in 'roles' too
    return router.createUrlTree(['/login']);
  }

  return true;
};