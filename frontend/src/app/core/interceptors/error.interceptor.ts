import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';
import { APP_CONFIG } from '../config/app-config';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const toastService = inject(ToastService);
  const router = inject(Router);
  const config = inject(APP_CONFIG);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const baseOrigin = config.apiBaseUrl
        ? new URL(config.apiBaseUrl).origin
        : (typeof window === 'undefined' ? 'http://localhost' : window.location.origin);
      const requestUrl = new URL(req.url, baseOrigin);
      const isApiRequest = typeof window !== 'undefined' && requestUrl.origin === baseOrigin && requestUrl.pathname.startsWith('/api/');
      const isAuthEndpoint = requestUrl.pathname.startsWith('/api/v1/auth/');

      if (isApiRequest && error.status === 401 && !isAuthEndpoint && authService.isLoggedIn()) {
        return authService.refreshSession().pipe(
          catchError(() => {
            authService.clearSession();
            toastService.warning('انتهت صلاحية الجلسة، يرجى تسجيل الدخول مرة أخرى.');
            router.navigate(['/login'], { queryParams: { returnUrl: router.url } });
            return throwError(() => error);
          }),
          switchMap(() => next(req.clone({ withCredentials: true })))
        );
      }

      if (isApiRequest && error.status === 403 && !isAuthEndpoint) {
        toastService.error('غير مسموح لك بتنفيذ هذا الإجراء حسب صلاحيات حسابك.');
      }

      return throwError(() => error);
    })
  );
};
