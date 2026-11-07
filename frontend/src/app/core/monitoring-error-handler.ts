import { ErrorHandler, Inject, Injectable } from '@angular/core';
import { APP_CONFIG, AppConfig } from './config/app-config';

@Injectable()
export class MonitoringErrorHandler implements ErrorHandler {
  constructor(@Inject(APP_CONFIG) private readonly config: AppConfig) {}

  handleError(error: unknown): void {
    console.error(error);
    if (!this.config.sentryDsn) return;
    void import('@sentry/angular').then(Sentry => Sentry.captureException(error));
  }
}
