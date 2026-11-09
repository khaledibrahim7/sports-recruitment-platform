import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { ApiResponse } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class VisitorService {
  private readonly API_URL = '/api/v1/visitors';
  private readonly http = inject(HttpClient);

  public visitorCountSignal = signal<number>(0);

  public registerVisit(): Observable<ApiResponse<number>> {
    return this.http.post<ApiResponse<number>>(`${this.API_URL}/visit`, {}).pipe(
      tap(res => {
        if (res.success && res.data !== undefined && res.data !== null) {
          this.visitorCountSignal.set(res.data);
        }
      })
    );
  }

  public getVisitorCount(): Observable<ApiResponse<number>> {
    return this.http.get<ApiResponse<number>>(`${this.API_URL}/count`).pipe(
      tap(res => {
        if (res.success && res.data !== undefined && res.data !== null) {
          this.visitorCountSignal.set(res.data);
        }
      })
    );
  }
}
