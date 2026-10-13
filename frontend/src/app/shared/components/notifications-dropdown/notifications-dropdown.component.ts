import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../../core/services/notification.service';
import { NotificationItem } from '../../../core/models/models';

@Component({
  selector: 'app-notifications-dropdown',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="notifications-wrapper position-relative">
      <button type="button" (click)="toggleDropdown()" class="btn-notif-bell">
        <i class="fa-solid fa-bell"></i>
        <span class="notif-label">الإشعارات</span>
        @if (unreadCount() > 0) {
          <span class="notif-badge">{{ unreadCount() }}</span>
        }
      </button>

      @if (isOpen()) {
        <div class="notif-dropdown glass-card p-3" (click)="$event.stopPropagation()">
          <div class="d-flex justify-content-between align-items-center mb-2 pb-2 border-bottom">
            <h4 class="m-0 text-sm font-bold">
              <i class="fa-solid fa-bell text-gold"></i> الإشعارات والفرص
            </h4>
            @if (notifications().length > 0) {
              <button (click)="markAllAsRead()" class="btn-text text-xs text-primary">
                تمييز الكل كمقروء
              </button>
            }
          </div>

          @if (notifications().length === 0) {
            <div class="py-4 text-center text-muted text-xs">
              <i class="fa-solid fa-bell-slash text-lg mb-1 d-block opacity-50"></i>
              لا توجد إشعارات جديدة حتى الآن
            </div>
          } @else {
            <div class="notif-list">
              @for (notif of notifications(); track notif.id) {
                <div 
                  class="notif-item p-2 mb-2 rounded cursor-pointer transition-all" 
                  [class.unread]="!notif.isRead"
                  (click)="markAsRead(notif)"
                >
                  <div class="d-flex justify-content-between align-items-start gap-2">
                    <strong class="notif-title text-xs text-white d-block">{{ notif.title }}</strong>
                    <span class="text-xs text-muted font-mono whitespace-nowrap">{{ notif.createdAt | date:'shortTime' }}</span>
                  </div>
                  <p class="notif-msg text-xs text-muted m-0 mt-1 line-clamp-2">{{ notif.message }}</p>
                </div>
              }
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .btn-notif-bell {
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
      padding: 0.38rem 0.85rem;
      border-radius: 8px;
      font-size: 0.8rem;
      font-weight: 700;
      color: var(--text-main);
      background: rgba(255, 255, 255, 0.06);
      border: 1px solid rgba(255, 255, 255, 0.14);
      cursor: pointer;
      position: relative;
      transition: all 0.2s ease;
      white-space: nowrap;

      i {
        font-size: 0.9rem;
      }

      &:hover {
        background: rgba(0, 255, 135, 0.1);
        border-color: rgba(0, 255, 135, 0.35);
        color: var(--primary-color);
      }
    }
    .notif-label {
      font-family: 'Cairo', sans-serif;
    }
    .notif-badge {
      position: absolute;
      top: -3px;
      right: -3px;
      background: #ef4444;
      color: #ffffff;
      font-size: 0.7rem;
      font-weight: 800;
      width: 18px;
      height: 18px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 2px solid var(--bg-dark);
    }
    .notif-dropdown {
      position: absolute;
      top: 120%;
      left: 0;
      width: 320px;
      max-height: 400px;
      overflow-y: auto;
      z-index: 1050;
      background: rgba(19, 27, 46, 0.98);
      border: 1px solid var(--border-glow);
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.6);
    }
    .btn-text {
      background: transparent;
      border: none;
      cursor: pointer;

      &:hover {
        text-decoration: underline;
      }
    }
    .notif-item {
      background: rgba(255, 255, 255, 0.03);

      &:hover {
        background: rgba(255, 255, 255, 0.07);
      }

      &.unread {
        border-right: 3px solid var(--primary-color);
        background: rgba(16, 185, 129, 0.08);
      }
    }
  `]
})
export class NotificationsDropdownComponent implements OnInit {
  public isOpen = signal(false);

  constructor(public notificationService: NotificationService) {}

  ngOnInit(): void {
    this.notificationService.loadNotifications();
  }

  public get unreadCount() {
    return this.notificationService.unreadCountSignal;
  }

  public get notifications() {
    return this.notificationService.notificationsSignal;
  }

  public toggleDropdown(): void {
    this.isOpen.set(!this.isOpen());
    if (this.isOpen()) {
      this.notificationService.loadNotifications();
    }
  }

  public markAsRead(notif: NotificationItem): void {
    if (!notif.isRead) {
      this.notificationService.markAsRead(notif.id).subscribe();
    }
  }

  public markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe();
  }
}
