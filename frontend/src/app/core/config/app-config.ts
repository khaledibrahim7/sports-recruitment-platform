import { InjectionToken } from '@angular/core';

/**
 * Central application configuration token.
 * The API base URL is read from a runtime config injected at bootstrap —
 * NEVER hardcoded in source code.
 *
 * In development: proxied through angular.json proxy to http://localhost:8080
 * In production:  set window.__APP_CONFIG__.apiBaseUrl before Angular boots
 */
export interface AppConfig {
  apiBaseUrl: string;
  sentryDsn?: string;
  environment?: string;
}

export const APP_CONFIG = new InjectionToken<AppConfig>('APP_CONFIG');

/**
 * Reads config from the global window object (set by the server/deploy pipeline).
 * Falls back to empty string so all requests go through the Angular dev proxy.
 */
export function appConfigFactory(): AppConfig {
  const win = typeof window === 'undefined' ? undefined : window as Window & { __APP_CONFIG__?: AppConfig };
  const nodeEnv = (globalThis as { process?: { env?: Record<string, string | undefined> } }).process?.env;
  const runtime = win?.__APP_CONFIG__ ?? (nodeEnv ? {
    apiBaseUrl: nodeEnv['API_BASE_URL'],
    sentryDsn: nodeEnv['SENTRY_DSN'],
    environment: nodeEnv['NODE_ENV'],
  } : undefined);
  let apiBaseUrl = '';
  try {
    const candidate = runtime?.apiBaseUrl?.trim();
    if (candidate) {
      const parsed = new URL(candidate);
      if ((parsed.protocol === 'https:' || (parsed.protocol === 'http:' && ['localhost', '127.0.0.1'].includes(parsed.hostname)))
        && !parsed.username && !parsed.password && !parsed.search && !parsed.hash
        && (!parsed.pathname || parsed.pathname === '/')) apiBaseUrl = parsed.origin;
    }
  } catch { /* Invalid API configuration falls back to the same-origin proxy. */ }
  return {
    apiBaseUrl,
    sentryDsn: runtime?.sentryDsn,
    environment: runtime?.environment
  };
}
