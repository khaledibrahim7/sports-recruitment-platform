import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { ApiResponse, PublicPlayerProfile } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class BookmarkService {
  private readonly API_URL = '/api/v1/bookmarks';

  public bookmarkedIdsSignal = signal<number[]>([]);

  constructor(private http: HttpClient) {}

  public toggleBookmark(playerId: number): Observable<ApiResponse<{ bookmarked: boolean }>> {
    return this.http.post<ApiResponse<{ bookmarked: boolean }>>(`${this.API_URL}/${playerId}/toggle`, {}).pipe(
      tap(res => {
        if (res.success && res.data) {
          const current = this.bookmarkedIdsSignal();
          if (res.data.bookmarked) {
            this.bookmarkedIdsSignal.set([...current, playerId]);
          } else {
            this.bookmarkedIdsSignal.set(current.filter(id => id !== playerId));
          }
        }
      })
    );
  }

  public getBookmarkedPlayers(): Observable<ApiResponse<PublicPlayerProfile[]>> {
    return this.http.get<ApiResponse<PublicPlayerProfile[]>>(this.API_URL);
  }

  public loadBookmarkedIds(): void {
    this.http.get<ApiResponse<number[]>>(`${this.API_URL}/ids`).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.bookmarkedIdsSignal.set(res.data);
        }
      },
      error: () => {}
    });
  }

  public isBookmarked(playerId: number): boolean {
    return this.bookmarkedIdsSignal().includes(playerId);
  }
}
