import { Component, EventEmitter, Input, Output } from '@angular/core';
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
        <div class="modal-card glass-card p-4" (click)="$event.stopPropagation()">
          <div class="modal-header d-flex justify-content-between align-items-center mb-3">
            <h3 class="m-0">
              <i class="fa-solid fa-paper-plane text-primary"></i> إرسال دعوة معايشة / اختبار للاعب
            </h3>
            <button type="button" (click)="onClose()" class="btn-close-modal">&times;</button>
          </div>

          <p class="text-muted text-sm mb-3">
            اللاعب المستهدف: <strong>{{ playerName }}</strong>
          </p>

          @if (successMessage) {
            <div class="alert alert-success py-2 px-3 mb-3 text-sm">
              <i class="fa-solid fa-circle-check"></i> {{ successMessage }}
            </div>
          }

          @if (errorMessage) {
            <div class="alert alert-danger py-2 px-3 mb-3 text-sm">
              <i class="fa-solid fa-circle-exclamation"></i> {{ errorMessage }}
            </div>
          }

          <form (ngSubmit)="sendOffer()">
            <div class="form-group">
              <label>تاريخ المعايشة / الاختبار *</label>
              <input type="date" [(ngModel)]="trialDate" name="trialDate" required class="form-control" />
            </div>

            <div class="form-group">
              <label>مكان وشبكة الملاعب (العنوان) *</label>
              <input 
                type="text" 
                [(ngModel)]="location" 
                name="location" 
                placeholder="مثال: ملاعب النادي - القاهرة - التجمع الخامس" 
                required 
                class="form-control" 
              />
            </div>

            <div class="form-group">
              <label>تفاصيل ودعوة المتابعة (اختياري)</label>
              <textarea 
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
  @Input() playerName: string = '';
  @Output() close = new EventEmitter<void>();

  public trialDate: string = '';
  public location: string = '';
  public details: string = '';

  public isSubmitting = false;
  public successMessage = '';
  public errorMessage = '';

  constructor(private trialOfferService: TrialOfferService) {}

  public sendOffer(): void {
    if (!this.playerId || !this.trialDate || !this.location) {
      this.errorMessage = 'يرجى إدخال كافة البيانات المطلوبة (*)';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.trialOfferService.sendOffer(this.playerId, this.trialDate, this.location, this.details).subscribe({
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
