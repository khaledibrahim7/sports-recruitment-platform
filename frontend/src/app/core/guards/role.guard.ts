import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Role } from '../models/models';

export const roleGuard: CanActivateFn = route => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const allowedRoles = route.data?.['roles'] as Role[] | undefined;
  const role = auth.userRole();

  if (role && allowedRoles?.includes(role)) return true;
  return router.createUrlTree([auth.isLoggedIn() ? '/' : '/login']);
};
