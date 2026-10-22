import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AcademyProfile, ApiResponse } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class AcademyService {
  private readonly API_URL = '/api/v1/academies';

  constructor(private http: HttpClient) {}

  public getAcademies(): Observable<ApiResponse<AcademyProfile[]>> {
    return this.http.get<ApiResponse<AcademyProfile[]>>(this.API_URL);
  }

  public getAcademyById(id: number): Observable<ApiResponse<AcademyProfile>> {
    return this.http.get<ApiResponse<AcademyProfile>>(`${this.API_URL}/${id}`);
  }

  public getMyProfile(): Observable<ApiResponse<AcademyProfile>> {
    return this.http.get<ApiResponse<AcademyProfile>>(`${this.API_URL}/me`);
  }

  public updateMyProfile(payload: any): Observable<ApiResponse<AcademyProfile>> {
    return this.http.put<ApiResponse<AcademyProfile>>(`${this.API_URL}/me`, payload);
  }
}
