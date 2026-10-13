import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, PlayerPosition, PlayerProfile } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class PlayerService {
  private readonly API_URL = '/api/v1/players';

  constructor(private http: HttpClient) {}

  public getPlayers(
    position?: PlayerPosition | '',
    age?: number | null
  ): Observable<ApiResponse<PlayerProfile[]>> {
    let params = new HttpParams();

    if (position) params = params.set('position', position);
    if (age !== undefined && age !== null) params = params.set('age', age.toString());

    return this.http.get<ApiResponse<PlayerProfile[]>>(this.API_URL, { params });
  }

  public getPlayerById(id: number): Observable<ApiResponse<PlayerProfile>> {
    return this.http.get<ApiResponse<PlayerProfile>>(`${this.API_URL}/${id}`);
  }

  public updateMyProfile(payload: any): Observable<ApiResponse<PlayerProfile>> {
    return this.http.put<ApiResponse<PlayerProfile>>(`${this.API_URL}/me`, payload);
  }
}
