import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  template: `
    @if (toastService.toasts().length > 0) {
      <div class="toast-container" role="region" aria-live="polite" aria-label="التنبيهات">
        @for (toast of toastService.toasts(); track toast.id) {
          <div class="toast-item" [class]="'toast-' + toast.type" role="alert">
            <i class="fa-solid"
               [class.fa-circle-exclamation]="toast.type === 'error'"
               [class.fa-triangle-exclamation]="toast.type === 'warning'"
               [class.fa-circle-check]="toast.type === 'success'"
               [class.fa-circle-info]="toast.type === 'info'"></i>
            <span class="toast-text">{{ toast.message }}</span>
            <button type="button" class="toast-close" (click)="toastService.dismiss(toast.id)" aria-label="إغلاق التنبيه">
              <i class="fa-solid fa-xmark"></i>
            </button>
          </div>
        }
      </div>
    }
  `,
  styles: [`
    .toast-container {
      position: fixed;
      top: 5rem;
      left: 1.25rem;
      z-index: 9500;
      display: flex;
      flex-direction: column;
      gap: 0.6rem;
      max-width: min(420px, calc(100vw - 2.5rem));
      pointer-events: none;
    }
    .toast-item {
      pointer-events: auto;
      display: flex;
      align-items: center;
      gap: 0.65rem;
      padding: 0.75rem 1rem;
      border-radius: var(--radius-sm, 8px);
      background: rgba(22, 22, 24, 0.96);
      border: 1px solid var(--rule, rgba(201,151,74,0.2));
      border-inline-start: 4px solid var(--acc, #C9974A);
      color: var(--ink, #EAE0D5);
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.55);
      font-size: 0.85rem;
      font-weight: 600;
      animation: fadeIn 0.2s ease;
    }
    .toast-error {
      border-inline-start-color: #ef4444;
      i:first-child { color: #ef4444; }
    }
    .toast-warning {
      border-inline-start-color: #f59e0b;
      i:first-child { color: #f59e0b; }
    }
    .toast-success {
      border-inline-start-color: #10b981;
      i:first-child { color: #10b981; }
    }
    .toast-info {
      border-inline-start-color: var(--acc, #C9974A);
      i:first-child { color: var(--acc, #C9974A); }
    }
    .toast-text {
      flex: 1;
      line-height: 1.45;
    }
    .toast-close {
      background: transparent;
      border: none;
      color: var(--mute, #7A7168);
      cursor: pointer;
      padding: 0.2rem;
      font-size: 0.9rem;
      line-height: 1;
      &:hover { color: var(--ink, #EAE0D5); }
    }
  `]
})
export class ToastComponent {
  public readonly toastService = inject(ToastService);
}
