import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, shareReplay, tap } from 'rxjs';
import { AcademyProfile, ApiResponse } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class AcademyService {
  private readonly API_URL = '/api/v1/academies';
  private readonly http = inject(HttpClient);
  private academiesCache$: Observable<ApiResponse<AcademyProfile[]>> | null = null;

  public getAcademies(forceRefresh = false): Observable<ApiResponse<AcademyProfile[]>> {
    if (forceRefresh || !this.academiesCache$) {
      this.academiesCache$ = this.http.get<ApiResponse<AcademyProfile[]>>(this.API_URL).pipe(
        tap({
          error: () => { this.academiesCache$ = null; }
        }),
        shareReplay({ bufferSize: 1, refCount: false })
      );
    }
    return this.academiesCache$;
  }

  public invalidateCache(): void {
    this.academiesCache$ = null;
  }

  public getAcademyById(id: number): Observable<ApiResponse<AcademyProfile>> {
    return this.http.get<ApiResponse<AcademyProfile>>(`${this.API_URL}/${id}`);
  }

  public getMyProfile(): Observable<ApiResponse<AcademyProfile>> {
    return this.http.get<ApiResponse<AcademyProfile>>(`${this.API_URL}/me`);
  }

  public updateMyProfile(payload: Partial<AcademyProfile>): Observable<ApiResponse<AcademyProfile>> {
    return this.http.put<ApiResponse<AcademyProfile>>(`${this.API_URL}/me`, payload).pipe(
      tap(() => this.invalidateCache())
    );
  }
}
