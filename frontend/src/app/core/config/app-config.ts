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
}

export const APP_CONFIG = new InjectionToken<AppConfig>('APP_CONFIG');

/**
 * Reads config from the global window object (set by the server/deploy pipeline).
 * Falls back to empty string so all requests go through the Angular dev proxy.
 */
export function appConfigFactory(): AppConfig {
  const win = window as unknown as { __APP_CONFIG__?: AppConfig };
  return {
    apiBaseUrl: win.__APP_CONFIG__?.apiBaseUrl ?? ''
  };
}
