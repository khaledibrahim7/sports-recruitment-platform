import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TrialOfferService } from '../../../core/services/trial-offer.service';

@Component({
  selector: 'app-trial-offer-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    @if (isOpen) {
      <div class="modal-backdrop" (click)="onClose()">
        <div class="modal-card glass-card p-4" role="dialog" aria-modal="true" aria-labelledby="trial-modal-title" (click)="$event.stopPropagation()">
          <div class="modal-header d-flex justify-content-between align-items-center mb-3">
            <h3 class="m-0" id="trial-modal-title">
              <i class="fa-solid fa-paper-plane text-primary"></i> إرسال دعوة معايشة / اختبار للاعب
            </h3>
            <button type="button" (click)="onClose()" class="btn-close-modal" aria-label="إغلاق نافذة دعوة المعايشة">&times;</button>
          </div>

          <p class="text-muted text-sm mb-3">
            اللاعب المستهدف: <strong>{{ playerName }}</strong>
          </p>

          @if (successMessage) {
            <div class="alert alert-success py-2 px-3 mb-3 text-sm" role="status">
              <i class="fa-solid fa-circle-check"></i> {{ successMessage }}
            </div>
          }

          @if (errorMessage) {
            <div class="alert alert-danger py-2 px-3 mb-3 text-sm" role="alert">
              <i class="fa-solid fa-circle-exclamation"></i> {{ errorMessage }}
            </div>
          }

          <form (ngSubmit)="sendOffer()">
            <div class="form-group">
              <label for="trialDateInput">تاريخ المعايشة / الاختبار *</label>
              <input id="trialDateInput" type="date" [(ngModel)]="trialDate" name="trialDate" required class="form-control" />
            </div>

            <div class="form-group">
              <label for="trialLocationInput">مكان وشبكة الملاعب (العنوان) *</label>
              <input
                id="trialLocationInput"
                type="text"
                [(ngModel)]="location"
                name="location"
                placeholder="مثال: ملاعب النادي - القاهرة - التجمع الخامس"
                required
                class="form-control"
              />
            </div>

            <div class="form-group">
              <label for="trialDetailsInput">تفاصيل ودعوة المتابعة (اختياري)</label>
              <textarea
                id="trialDetailsInput"
                [(ngModel)]="details"
                name="details"
                placeholder="أدخل تفاصيل الحضور، وقت الحضور، أو الملاحظات المطلوبة للاعب..."
                class="form-control"
              ></textarea>
            </div>

            <div class="d-flex gap-2 mt-4">
              <button type="submit" [disabled]="isSubmitting" class="btn btn-primary w-100">
                @if (isSubmitting) {
                  <i class="fa-solid fa-spinner fa-spin"></i> جاري الإرسال...
                } @else {
                  <i class="fa-solid fa-paper-plane"></i> إرسال الدعوة الآن
                }
              </button>
              <button type="button" (click)="onClose()" class="btn btn-outline">إلغاء</button>
            </div>
          </form>
        </div>
      </div>
    }
  `,
  styles: [`
    .modal-backdrop {
      position: fixed;
      inset: 0;
      background: rgba(0, 0, 0, 0.75);
      backdrop-filter: blur(8px);
      z-index: 1100;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 1rem;
    }
    .modal-card {
      width: 100%;
      max-width: 500px;
      border-radius: var(--radius-md);
      background: rgba(19, 27, 46, 0.95);
      border: 1px solid var(--border-glow);
    }
    .btn-close-modal {
      background: transparent;
      border: none;
      color: var(--text-muted);
      font-size: 1.8rem;
      cursor: pointer;
      line-height: 1;

      &:hover {
        color: #ffffff;
      }
    }
  `]
})
export class TrialOfferModalComponent {
  @Input() isOpen = false;
  @Input() playerId: number | null = null;
  @Input() playerName = '';
  @Output() close = new EventEmitter<void>();

  public trialDate = '';
  public location = '';
  public details = '';

  public isSubmitting = false;
  public successMessage = '';
  public errorMessage = '';

  constructor(private trialOfferService: TrialOfferService) {}

  @HostListener('document:keydown.escape')
  public handleEscape(): void {
    if (this.isOpen) {
      this.onClose();
    }
  }

  public sendOffer(): void {
    if (!this.playerId || !this.trialDate || !this.location.trim()) {
      this.errorMessage = 'يرجى إدخال كافة البيانات المطلوبة (*)';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.trialOfferService.sendOffer(this.playerId, this.trialDate, this.location.trim(), this.details.trim()).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        if (res.success) {
          this.successMessage = 'تم إرسال دعوة المعايشة والاختبار للاعب بنجاح!';
          setTimeout(() => {
            this.onClose();
          }, 1500);
        }
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.message || 'فشل إرسال الطلب، يرجى المحاولة مجدداً.';
      }
    });
  }

  public onClose(): void {
    this.trialDate = '';
    this.location = '';
    this.details = '';
    this.successMessage = '';
    this.errorMessage = '';
    this.close.emit();
  }
}
