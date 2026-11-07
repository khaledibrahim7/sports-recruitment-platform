import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { APP_CONFIG } from '../config/app-config';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const config = inject(APP_CONFIG);
  if (!req.url.startsWith('/api/')) return next(req);
  if (typeof window === 'undefined' && !config.apiBaseUrl) return next(req);
  const baseOrigin = config.apiBaseUrl || window.location.origin;
  const target = new URL(req.url, baseOrigin);
  const cloned = req.clone({ url: target.toString(), withCredentials: true });
  return next(cloned);
};
