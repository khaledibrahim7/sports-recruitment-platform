import { Injectable, signal } from '@angular/core';

export type ToastType = 'error' | 'warning' | 'success' | 'info';

export interface ToastMessage {
  id: number;
  message: string;
  type: ToastType;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private nextId = 1;
  public readonly toasts = signal<ToastMessage[]>([]);

  public show(message: string, type: ToastType = 'info', durationMs = 4500): void {
    const trimmed = message?.trim();
    if (!trimmed) return;
    // Prevent duplicate identical toast spam
    if (this.toasts().some(t => t.message === trimmed && t.type === type)) return;

    const id = this.nextId++;
    this.toasts.update(list => [...list, { id, message: trimmed, type }]);

    if (durationMs > 0 && typeof window !== 'undefined') {
      window.setTimeout(() => this.dismiss(id), durationMs);
    }
  }

  public error(message: string, durationMs = 5000): void {
    this.show(message, 'error', durationMs);
  }

  public warning(message: string, durationMs = 4500): void {
    this.show(message, 'warning', durationMs);
  }

  public success(message: string, durationMs = 3500): void {
    this.show(message, 'success', durationMs);
  }

  public dismiss(id: number): void {
    this.toasts.update(list => list.filter(t => t.id !== id));
  }
}
