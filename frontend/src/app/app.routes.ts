import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent) },
  { path: 'login', loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent), canActivate: [guestGuard] },
  { path: 'register', loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent), canActivate: [guestGuard] },
  { path: 'verify-account', loadComponent: () => import('./features/auth/verify-account/verify-account.component').then(m => m.VerifyAccountComponent) },
  { path: 'players', loadComponent: () => import('./features/players/player-list/player-list.component').then(m => m.PlayerListComponent) },
  { path: 'players/:id', loadComponent: () => import('./features/players/player-public-profile/player-public-profile.component').then(m => m.PlayerPublicProfileComponent) },
  { path: 'academies', loadComponent: () => import('./features/academies/academy-list/academy-list.component').then(m => m.AcademyListComponent) },
  { path: 'clubs', loadComponent: () => import('./features/clubs/club-list/club-list.component').then(m => m.ClubListComponent) },
  { path: 'profile', loadComponent: () => import('./features/profile/profile-editor/profile-editor.component').then(m => m.ProfileEditorComponent), canActivate: [authGuard, roleGuard], data: { roles: ['PLAYER', 'ACADEMY', 'CLUB'] } },
  { path: '**', redirectTo: '' }
];
