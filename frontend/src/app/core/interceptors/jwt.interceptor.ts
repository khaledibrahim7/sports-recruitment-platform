import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  const requestUrl = new URL(req.url, typeof window === 'undefined' ? 'http://localhost' : window.location.origin);
  const isSameOriginApi = typeof window !== 'undefined' && requestUrl.origin === window.location.origin && requestUrl.pathname.startsWith('/api/');

  if (token && isSameOriginApi) {
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(cloned);
  }

  return next(req);
};
