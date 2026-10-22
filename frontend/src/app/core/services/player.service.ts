import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, PlayerPosition, PlayerProfile, PlayerSearchPage, PublicPlayerProfile } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class PlayerService {
  private readonly API_URL = '/api/v1/players';

  constructor(private http: HttpClient) {}

  public getPlayers(
    position?: PlayerPosition | '',
    age?: number | null,
    query = '',
    page = 0,
    size = 12
  ): Observable<ApiResponse<PlayerSearchPage>> {
    let params = new HttpParams().set('page', page).set('size', size);

    if (position) params = params.set('position', position);
    if (age !== undefined && age !== null) params = params.set('age', age.toString());
    if (query.trim()) params = params.set('query', query.trim());

    return this.http.get<ApiResponse<PlayerSearchPage>>(this.API_URL, { params });
  }

  public searchPlayers(position: PlayerPosition | '', age: number | null, query: string, page: number, size = 12): Observable<ApiResponse<PlayerSearchPage>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (position) params = params.set('position', position);
    if (age !== null) params = params.set('age', age);
    if (query.trim()) params = params.set('query', query.trim());
    return this.http.get<ApiResponse<PlayerSearchPage>>(this.API_URL, { params });
  }

  public getPlayerById(id: number): Observable<ApiResponse<PublicPlayerProfile>> {
    return this.http.get<ApiResponse<PublicPlayerProfile>>(`${this.API_URL}/${id}`);
  }

  public getMyProfile(): Observable<ApiResponse<PlayerProfile>> {
    return this.http.get<ApiResponse<PlayerProfile>>(`${this.API_URL}/me`);
  }

  public updateMyProfile(payload: any): Observable<ApiResponse<PlayerProfile>> {
    return this.http.put<ApiResponse<PlayerProfile>>(`${this.API_URL}/me`, payload);
  }

  public createVideoUploadUrl(file: File): Observable<ApiResponse<{ key: string; uploadUrl: string; contentType: string; maxBytes: number }>> {
    const params = new HttpParams().set('filename', file.name).set('contentType', file.type);
    return this.http.post<ApiResponse<{ key: string; uploadUrl: string; contentType: string; maxBytes: number }>>(`${this.API_URL}/me/videos/upload-url`, null, { params });
  }

  public uploadVideoToStorage(uploadUrl: string, file: File, contentType: string): Observable<unknown> {
    return this.http.put(uploadUrl, file, { headers: { 'Content-Type': contentType }, responseType: 'text' });
  }

  public confirmVideoUpload(key: string): Observable<ApiResponse<string>> {
    const params = new HttpParams().set('key', key);
    return this.http.post<ApiResponse<string>>(`${this.API_URL}/me/videos/confirm`, null, { params });
  }

  public resolveVideoUrl(key: string): Observable<ApiResponse<string>> {
    return this.http.get<ApiResponse<string>>(`${this.API_URL}/videos/${key}`);
  }

  public deleteUploadedVideo(key: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.API_URL}/me/videos/${encodeURIComponent(key)}`);
  }

  public deleteRemovedVideos(keys: string[]): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.API_URL}/me/videos`, { body: keys });
  }
}
