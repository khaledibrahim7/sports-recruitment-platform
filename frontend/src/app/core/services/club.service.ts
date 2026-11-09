import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, shareReplay, tap } from 'rxjs';
import { ApiResponse, ClubProfile } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class ClubService {
  private readonly API_URL = '/api/v1/clubs';
  private clubsCache$: Observable<ApiResponse<ClubProfile[]>> | null = null;

  constructor(private http: HttpClient) {}

  public getClubs(forceRefresh = false): Observable<ApiResponse<ClubProfile[]>> {
    if (forceRefresh || !this.clubsCache$) {
      this.clubsCache$ = this.http.get<ApiResponse<ClubProfile[]>>(this.API_URL).pipe(
        tap({
          error: () => { this.clubsCache$ = null; }
        }),
        shareReplay({ bufferSize: 1, refCount: false })
      );
    }
    return this.clubsCache$;
  }

  public invalidateCache(): void {
    this.clubsCache$ = null;
  }

  public getClubById(id: number): Observable<ApiResponse<ClubProfile>> {
    return this.http.get<ApiResponse<ClubProfile>>(`${this.API_URL}/${id}`);
  }

  public getMyProfile(): Observable<ApiResponse<ClubProfile>> {
    return this.http.get<ApiResponse<ClubProfile>>(`${this.API_URL}/me`);
  }

  public updateMyProfile(payload: Partial<ClubProfile>): Observable<ApiResponse<ClubProfile>> {
    return this.http.put<ApiResponse<ClubProfile>>(`${this.API_URL}/me`, payload).pipe(
      tap(() => this.invalidateCache())
    );
  }
}
