import { appConfigFactory } from './app/core/config/app-config';

export function initializeSentry(): void {
  const config = appConfigFactory();
  if (!config?.sentryDsn) return;
  void import('@sentry/angular').then(Sentry => Sentry.init({
    dsn: config.sentryDsn,
    environment: config.environment || 'production',
    dataCollection: {
      userInfo: false,
      cookies: false,
      httpHeaders: false,
      httpBodies: [],
      urlQueryParams: false,
    },
    tracesSampleRate: 0.05,
    beforeSend(event) {
      event.user = undefined;
      if (event.request) {
        if (event.request.url) event.request.url = event.request.url.split('?')[0];
        event.request.headers = undefined;
        event.request.cookies = undefined;
        event.request.data = undefined;
      }
      return event;
    },
  }));
}
