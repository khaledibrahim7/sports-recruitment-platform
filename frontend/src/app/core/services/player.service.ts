import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, shareReplay, tap } from 'rxjs';
import { ApiResponse, PlayerPosition, PlayerProfile, PlayerSearchPage, PublicPlayerProfile } from '../models/models';
import { APP_CONFIG, AppConfig } from '../config/app-config';

@Injectable({
  providedIn: 'root'
})
export class PlayerService {
  private readonly API_URL = '/api/v1/players';
  private readonly http = inject(HttpClient);
  private readonly config: AppConfig = inject(APP_CONFIG);
  private readonly cache = new Map<string, { expiresAt: number; observable$: Observable<ApiResponse<PlayerSearchPage>> }>();
  private readonly CACHE_TTL_MS = 60_000;

  public getPlayers(
    position?: PlayerPosition | '',
    age?: number | null,
    query = '',
    page = 0,
    size = 12,
    forceRefresh = false
  ): Observable<ApiResponse<PlayerSearchPage>> {
    const trimmedQuery = query.trim();
    const isCacheable = !position && (age === undefined || age === null) && !trimmedQuery && page === 0;
    const cacheKey = `default:${size}`;

    if (isCacheable && !forceRefresh) {
      const cached = this.cache.get(cacheKey);
      if (cached && cached.expiresAt > Date.now()) {
        return cached.observable$;
      }
    }

    let params = new HttpParams().set('page', page).set('size', size);
    if (position) params = params.set('position', position);
    if (age !== undefined && age !== null) params = params.set('age', age.toString());
    if (trimmedQuery) params = params.set('query', trimmedQuery);

    const request$ = this.http.get<ApiResponse<PlayerSearchPage>>(this.API_URL, { params }).pipe(
      tap({
        error: () => {
          if (isCacheable) this.cache.delete(cacheKey);
        }
      }),
      shareReplay({ bufferSize: 1, refCount: false })
    );

    if (isCacheable) {
      this.cache.set(cacheKey, { expiresAt: Date.now() + this.CACHE_TTL_MS, observable$: request$ });
    }

    return request$;
  }

  public invalidateCache(): void {
    this.cache.clear();
  }

  public searchPlayers(position: PlayerPosition | '', age: number | null, query: string, page: number, size = 12): Observable<ApiResponse<PlayerSearchPage>> {
    return this.getPlayers(position, age, query, page, size);
  }

  public getPlayerById(id: number): Observable<ApiResponse<PublicPlayerProfile>> {
    return this.http.get<ApiResponse<PublicPlayerProfile>>(`${this.API_URL}/${id}`);
  }

  public getMyProfile(): Observable<ApiResponse<PlayerProfile>> {
    return this.http.get<ApiResponse<PlayerProfile>>(`${this.API_URL}/me`);
  }

  public updateMyProfile(payload: Record<string, unknown>): Observable<ApiResponse<PlayerProfile>> {
    return this.http.put<ApiResponse<PlayerProfile>>(`${this.API_URL}/me`, payload).pipe(
      tap(() => this.invalidateCache())
    );
  }

  public createVideoUploadUrl(file: File): Observable<ApiResponse<{ key: string; uploadUrl: string; contentType: string; maxBytes: number }>> {
    const params = new HttpParams().set('filename', file.name).set('contentType', file.type);
    return this.http.post<ApiResponse<{ key: string; uploadUrl: string; contentType: string; maxBytes: number }>>(`${this.API_URL}/me/videos/upload-url`, null, { params });
  }

  public createImageUploadUrl(file: File): Observable<ApiResponse<{ key: string; uploadUrl: string; contentType: string; maxBytes: number }>> {
    const params = new HttpParams().set('filename', file.name).set('contentType', file.type);
    return this.http.post<ApiResponse<{ key: string; uploadUrl: string; contentType: string; maxBytes: number }>>('/api/v1/profile-media/images/upload-url', null, { params });
  }

  public confirmImageUpload(key: string): Observable<ApiResponse<string>> {
    const params = new HttpParams().set('key', key);
    return this.http.post<ApiResponse<string>>('/api/v1/profile-media/images/confirm', null, { params });
  }

  public imageUrl(value?: string | null): string {
    if (!value) return 'default-avatar.png';
    if (!value.startsWith('platform-image:')) return value;
    const key = value.substring('platform-image:'.length);
    return `${this.config.apiBaseUrl || ''}/api/v1/profile-media/images?key=${encodeURIComponent(key)}`;
  }

  public uploadFileToStorage(uploadUrl: string, file: File, contentType: string): Observable<unknown> {
    return this.http.put(uploadUrl, file, { headers: { 'Content-Type': contentType }, responseType: 'text' });
  }

  public confirmVideoUpload(key: string): Observable<ApiResponse<string>> {
    const params = new HttpParams().set('key', key);
    return this.http.post<ApiResponse<string>>(`${this.API_URL}/me/videos/confirm`, null, { params });
  }

  public resolveVideoUrl(key: string): Observable<ApiResponse<string>> {
    const params = new HttpParams().set('key', key);
    return this.http.get<ApiResponse<string>>(`${this.API_URL}/videos`, { params });
  }

  public deleteUploadedVideo(key: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.API_URL}/me/videos/${encodeURIComponent(key)}`);
  }

  public deleteRemovedVideos(keys: string[]): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.API_URL}/me/videos`, { body: keys });
  }
}
