import { ApplicationConfig, ErrorHandler, inject, PLATFORM_ID, provideAppInitializer, provideZoneChangeDetection } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { AuthService } from './core/services/auth.service';
import { APP_CONFIG, appConfigFactory } from './core/config/app-config';
import { MonitoringErrorHandler } from './core/monitoring-error-handler';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    { provide: APP_CONFIG, useFactory: appConfigFactory },
    { provide: ErrorHandler, useClass: MonitoringErrorHandler },
    provideAppInitializer(() => isPlatformBrowser(inject(PLATFORM_ID))
      ? inject(AuthService).restoreSession()
      : Promise.resolve()),
    provideRouter(routes),
    provideHttpClient(withInterceptors([jwtInterceptor, errorInterceptor])), provideClientHydration(withEventReplay())
  ]
};
