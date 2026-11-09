import { Injectable, inject, PLATFORM_ID, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Observable, Subscription, interval, tap } from 'rxjs';
import { ApiResponse, NotificationItem } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly API_URL = '/api/v1/notifications';
  private readonly platformId = inject(PLATFORM_ID);
  private pollingSub: Subscription | null = null;

  public unreadCountSignal = signal<number>(0);
  public notificationsSignal = signal<NotificationItem[]>([]);
  public loadErrorSignal = signal('');

  constructor(private http: HttpClient) {}

  public startPolling(intervalMs = 60_000): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.stopPolling();
    this.loadNotifications();
    this.pollingSub = interval(intervalMs).subscribe(() => {
      if (typeof document === 'undefined' || document.visibilityState === 'visible') {
        this.loadNotifications();
      }
    });
  }

  public stopPolling(): void {
    this.pollingSub?.unsubscribe();
    this.pollingSub = null;
  }

  public loadNotifications(): void {
    this.loadErrorSignal.set('');
    this.http.get<ApiResponse<NotificationItem[]>>(this.API_URL).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.notificationsSignal.set(res.data);
          const unread = res.data.filter(n => !n.isRead).length;
          this.unreadCountSignal.set(unread);
        } else {
          this.loadErrorSignal.set('تعذر تحميل الإشعارات. حاول مرة أخرى.');
        }
      },
      error: () => this.loadErrorSignal.set('تعذر تحميل الإشعارات. تحقق من الاتصال وحاول مرة أخرى.')
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
