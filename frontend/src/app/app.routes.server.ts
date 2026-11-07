import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  { path: '', renderMode: RenderMode.Prerender },
  { path: 'players', renderMode: RenderMode.Prerender },
  { path: 'players/:id', renderMode: RenderMode.Server },
  { path: 'academies', renderMode: RenderMode.Prerender },
  { path: 'clubs', renderMode: RenderMode.Prerender },
  { path: 'login', renderMode: RenderMode.Prerender },
  { path: 'register', renderMode: RenderMode.Prerender },
  { path: 'verify-account', renderMode: RenderMode.Client },
  { path: 'profile', renderMode: RenderMode.Client },
  { path: '**', renderMode: RenderMode.Client },
];
