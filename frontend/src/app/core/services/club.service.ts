import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, ClubProfile } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class ClubService {
  private readonly API_URL = '/api/v1/clubs';

  constructor(private http: HttpClient) {}

  public getClubs(): Observable<ApiResponse<ClubProfile[]>> {
    return this.http.get<ApiResponse<ClubProfile[]>>(this.API_URL);
  }

  public getClubById(id: number): Observable<ApiResponse<ClubProfile>> {
    return this.http.get<ApiResponse<ClubProfile>>(`${this.API_URL}/${id}`);
  }

  public updateMyProfile(payload: any): Observable<ApiResponse<ClubProfile>> {
    return this.http.put<ApiResponse<ClubProfile>>(`${this.API_URL}/me`, payload);
  }
}
