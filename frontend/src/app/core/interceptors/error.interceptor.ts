import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const requestUrl = new URL(req.url, typeof window === 'undefined' ? 'http://localhost' : window.location.origin);
      const isSameOriginApi = typeof window !== 'undefined' && requestUrl.origin === window.location.origin && requestUrl.pathname.startsWith('/api/');
      if (isSameOriginApi && error.status === 401) {
        // A forbidden response can be a valid authenticated user lacking permission.
        if (authService.isLoggedIn()) {
          authService.logout();
          router.navigate(['/login'], { queryParams: { returnUrl: router.url } });
        }
      }
      return throwError(() => error);
    })
  );
};
