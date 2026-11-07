import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, finalize, map, of, shareReplay, tap, throwError } from 'rxjs';
import { ApiResponse, AuthResponse } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = '/api/v1/auth';

  public currentUserSignal = signal<AuthResponse | null>(null);
  public isLoggedIn = computed(() => !!this.currentUserSignal());
  public userRole = computed(() => this.currentUserSignal()?.role || null);
  private refreshInFlight: Observable<ApiResponse<AuthResponse>> | null = null;

  constructor(private http: HttpClient) {}

  public register(payload: any): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.API_URL}/register`, {
      ...payload,
      email: typeof payload?.email === 'string' ? payload.email.trim().toLowerCase() : payload?.email
    });
  }

  public verifyAccount(token: string, kind: 'email' | 'guardian'): Observable<ApiResponse<void>> {
    const endpoint = kind === 'guardian' ? 'verify-guardian' : 'verify-email';
    return this.http.post<ApiResponse<void>>(`${this.API_URL}/${endpoint}`, { token });
  }

  public resendVerification(email: string): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.API_URL}/resend-verification`, { email: email.trim().toLowerCase() });
  }

  public login(payload: any): Observable<ApiResponse<AuthResponse>> {
    const normalizedPayload = {
      ...payload,
      email: typeof payload?.email === 'string' ? payload.email.trim().toLowerCase() : payload?.email
    };
    return this.http.post<ApiResponse<AuthResponse>>(`${this.API_URL}/login`, normalizedPayload).pipe(
      tap(res => {
        if (res.success && res.data) {
          this.setSession(res.data);
        }
      })
    );
  }

  public logout(): void {
    this.http.post<void>(`${this.API_URL}/logout`, {}, { withCredentials: true }).subscribe({ error: () => undefined });
    this.clearSession();
  }

  public clearSession(): void {
    this.currentUserSignal.set(null);
  }

  public getToken(): string | null {
    // Access and refresh tokens are HttpOnly cookies. JavaScript must never read them.
    return null;
  }

  public restoreSession(): Observable<null> {
    return this.refreshSession().pipe(
      map(() => null),
      catchError(() => { this.currentUserSignal.set(null); return of(null); })
    );
  }

  public refreshSession(): Observable<ApiResponse<AuthResponse>> {
    if (this.refreshInFlight) return this.refreshInFlight;
    const request = this.http.post<ApiResponse<AuthResponse>>(`${this.API_URL}/refresh`, {}, { withCredentials: true }).pipe(
      tap(res => {
        if (!res.success || !res.data) throw new Error('Session refresh returned no user');
        this.currentUserSignal.set(res.data);
      }),
      catchError(error => {
        this.currentUserSignal.set(null);
        return throwError(() => error);
      }),
      finalize(() => { this.refreshInFlight = null; }),
      shareReplay({ bufferSize: 1, refCount: false })
    );
    this.refreshInFlight = request;
    return request;
  }

  public updateProfileInSession(profileData: any): void {
    const current = this.currentUserSignal();
    if (current) {
      const updated = { ...current, profile: profileData };
      this.setSession(updated);
    }
  }

  private setSession(authData: AuthResponse): void {
    this.currentUserSignal.set(authData);
  }

}
