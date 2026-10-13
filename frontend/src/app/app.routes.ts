import { Routes } from '@angular/router';
import { HomeComponent } from './features/home/home.component';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { PlayerListComponent } from './features/players/player-list/player-list.component';
import { AcademyListComponent } from './features/academies/academy-list/academy-list.component';
import { ClubListComponent } from './features/clubs/club-list/club-list.component';
import { ProfileEditorComponent } from './features/profile/profile-editor/profile-editor.component';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent, canActivate: [guestGuard] },
  { path: 'register', component: RegisterComponent, canActivate: [guestGuard] },
  { path: 'players', component: PlayerListComponent },
  { path: 'academies', component: AcademyListComponent },
  { path: 'clubs', component: ClubListComponent },
  { path: 'profile', component: ProfileEditorComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: '' }
];
