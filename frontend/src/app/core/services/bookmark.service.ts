import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { ApiResponse, PublicPlayerProfile } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class BookmarkService {
  private readonly API_URL = '/api/v1/bookmarks';

  public bookmarkedIdsSignal = signal<number[]>([]);
  public loadErrorSignal = signal('');
  public actionErrorSignal = signal('');

  constructor(private http: HttpClient) {}

  public toggleBookmark(playerId: number): Observable<ApiResponse<{ bookmarked: boolean }>> {
    this.actionErrorSignal.set('');
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
      }),
      // The component can read a visible message instead of silently losing the action.
      // Keep the HTTP error propagated for callers that need to handle it.
      catchError(error => {
        this.actionErrorSignal.set('تعذر تحديث قائمة المتابعة. تحقق من الاتصال وحاول مرة أخرى.');
        return throwError(() => error);
      })
    );
  }

  public getBookmarkedPlayers(): Observable<ApiResponse<PublicPlayerProfile[]>> {
    return this.http.get<ApiResponse<PublicPlayerProfile[]>>(this.API_URL);
  }

  public loadBookmarkedIds(): void {
    this.loadErrorSignal.set('');
    this.http.get<ApiResponse<number[]>>(`${this.API_URL}/ids`).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.bookmarkedIdsSignal.set(res.data);
        }
      },
      error: () => this.loadErrorSignal.set('تعذر تحميل حالة قائمة المتابعة. حاول تحديث الصفحة.')
    });
  }

  public isBookmarked(playerId: number): boolean {
    return this.bookmarkedIdsSignal().includes(playerId);
  }
}
