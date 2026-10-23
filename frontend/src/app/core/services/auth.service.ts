import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { ApiResponse, AuthResponse } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = '/api/v1/auth';

  public currentUserSignal = signal<AuthResponse | null>(this.getStoredAuth());
  public isLoggedIn = computed(() => !!this.currentUserSignal());
  public userRole = computed(() => this.currentUserSignal()?.role || null);

  constructor(private http: HttpClient) {}

  public register(payload: any): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.API_URL}/register`, payload);
  }

  public verifyAccount(token: string, kind: 'email' | 'guardian'): Observable<ApiResponse<void>> {
    const endpoint = kind === 'guardian' ? 'verify-guardian' : 'verify-email';
    return this.http.post<ApiResponse<void>>(`${this.API_URL}/${endpoint}`, { token });
  }

  public resendVerification(email: string): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.API_URL}/resend-verification`, { email });
  }

  public login(payload: any): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.API_URL}/login`, payload).pipe(
      tap(res => {
        if (res.success && res.data) {
          this.setSession(res.data);
        }
      })
    );
  }

  public logout(): void {
    localStorage.removeItem('sports_auth_data');
    this.currentUserSignal.set(null);
  }

  public getToken(): string | null {
    return this.currentUserSignal()?.accessToken || null;
  }

  public updateProfileInSession(profileData: any): void {
    const current = this.currentUserSignal();
    if (current) {
      const updated = { ...current, profile: profileData };
      this.setSession(updated);
    }
  }

  private setSession(authData: AuthResponse): void {
    localStorage.setItem('sports_auth_data', JSON.stringify(authData));
    this.currentUserSignal.set(authData);
  }

  private getStoredAuth(): AuthResponse | null {
    try {
      const data = localStorage.getItem('sports_auth_data');
      return data ? JSON.parse(data) : null;
    } catch {
      return null;
    }
  }
}
