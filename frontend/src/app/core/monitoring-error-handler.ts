import { ErrorHandler, Injectable, inject } from '@angular/core';
import { APP_CONFIG } from './config/app-config';

@Injectable()
export class MonitoringErrorHandler implements ErrorHandler {
  private readonly config = inject(APP_CONFIG);

  handleError(error: unknown): void {
    console.error(error);
    if (!this.config.sentryDsn) return;
    void import('@sentry/angular').then(Sentry => Sentry.captureException(error));
  }
}
