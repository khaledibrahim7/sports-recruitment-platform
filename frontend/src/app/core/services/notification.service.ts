import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { ApiResponse, NotificationItem } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly API_URL = '/api/v1/notifications';

  public unreadCountSignal = signal<number>(0);
  public notificationsSignal = signal<NotificationItem[]>([]);

  constructor(private http: HttpClient) {}

  public loadNotifications(): void {
    this.http.get<ApiResponse<NotificationItem[]>>(this.API_URL).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.notificationsSignal.set(res.data);
          const unread = res.data.filter(n => !n.isRead).length;
          this.unreadCountSignal.set(unread);
        }
      },
      error: () => {}
    });
  }

  public markAsRead(id: number): Observable<ApiResponse<void>> {
    return this.http.patch<ApiResponse<void>>(`${this.API_URL}/${id}/read`, {}).pipe(
      tap(() => {
        const updated = this.notificationsSignal().map(n => n.id === id ? { ...n, isRead: true } : n);
        this.notificationsSignal.set(updated);
        const unread = updated.filter(n => !n.isRead).length;
        this.unreadCountSignal.set(unread);
      })
    );
  }

  public markAllAsRead(): Observable<ApiResponse<void>> {
    return this.http.patch<ApiResponse<void>>(`${this.API_URL}/read-all`, {}).pipe(
      tap(() => {
        const updated = this.notificationsSignal().map(n => ({ ...n, isRead: true }));
        this.notificationsSignal.set(updated);
        this.unreadCountSignal.set(0);
      })
    );
  }
}
