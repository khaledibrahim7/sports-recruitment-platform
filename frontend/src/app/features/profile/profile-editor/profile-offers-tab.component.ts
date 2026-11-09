import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Role, TrialOffer } from '../../../core/models/models';

@Component({
  selector: 'app-profile-offers-tab',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  template: `
    <div class="mt-4">
      <h3 class="mb-3 text-gold">
        <i class="fa-solid fa-paper-plane"></i>
        {{ userRole === 'PLAYER' ? 'طلبات المعايشة والاختبار الموجهة إليك' : 'طلبات المعايشة المنشورة للاعبين' }}
      </h3>

      @if (userRole === 'PLAYER') {
        @if (myOffers.length === 0) {
          <div class="text-center py-5 text-muted glass-card">
            <i class="fa-solid fa-envelope-open-text fa-3x mb-2 opacity-50"></i>
            <p>لم تتلقَ أي طلبات معايشة حتى الآن. تأكد من تحديث بروفايلك وفيديوهاتك لزيادة فرصة اكتشافك!</p>
          </div>
        } @else {
          <div class="offers-grid">
            @for (offer of myOffers; track offer.id) {
              <div class="glass-card p-3 mb-3 border-emerald">
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <span class="badge" [class.badge-academy]="offer.scoutRole === 'ACADEMY'" [class.badge-club]="offer.scoutRole === 'CLUB'">
                    <i class="fa-solid fa-building"></i> {{ offer.scoutName }} ({{ offer.scoutRole === 'ACADEMY' ? 'أكاديمية' : 'نادي' }})
                  </span>
                  <span class="badge" [class.bg-warning]="offer.status === 'PENDING'" [class.bg-success]="offer.status === 'ACCEPTED'" [class.bg-danger]="offer.status === 'REJECTED'">
                    {{ offer.status === 'PENDING' ? 'قيد الانتظار' : (offer.status === 'ACCEPTED' ? 'تم القبول ✅' : 'تم الرفض ❌') }}
                  </span>
                </div>

                <p class="mb-1 text-sm"><strong>التاريخ المحدد:</strong> {{ offer.trialDate }}</p>
                <p class="mb-1 text-sm"><strong>العنوان والمكان:</strong> {{ offer.location }}</p>
                @if (offer.details) {
                  <p class="text-muted text-xs mt-2 p-2 bg-dark rounded">{{ offer.details }}</p>
                }

                @if (offer.status === 'PENDING') {
                  <div class="d-flex gap-2 mt-3">
                    <button type="button" (click)="statusChange.emit({ offerId: offer.id, status: 'ACCEPTED' })" class="btn btn-primary btn-sm flex-grow-1">
                      <i class="fa-solid fa-check"></i> قبول الطلب
                    </button>
                    <button type="button" (click)="statusChange.emit({ offerId: offer.id, status: 'REJECTED' })" class="btn btn-outline-danger btn-sm">
                      <i class="fa-solid fa-xmark"></i> رفض
                    </button>
                  </div>
                }
              </div>
            }
          </div>
        }
      } @else {
        @if (sentOffers.length === 0) {
          <div class="text-center py-5 text-muted glass-card">
            <i class="fa-solid fa-paper-plane fa-3x mb-2 opacity-50"></i>
            <p>لم تقم بإرسال أي طلبات معايشة للاعبين بعد. استعرض سوق اللاعبين وأرسل دعوات معايشة للمواهب!</p>
          </div>
        } @else {
          <div class="offers-grid">
            @for (offer of sentOffers; track offer.id) {
              <div class="glass-card p-3 mb-3">
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <strong class="text-emerald"><i class="fa-solid fa-user-ninja"></i> اللاعب: {{ offer.playerName }}</strong>
                  <span class="badge" [class.bg-warning]="offer.status === 'PENDING'" [class.bg-success]="offer.status === 'ACCEPTED'" [class.bg-danger]="offer.status === 'REJECTED'">
                    {{ offer.status === 'PENDING' ? 'قيد الانتظار' : (offer.status === 'ACCEPTED' ? 'تم القبول ✅' : 'تم الرفض ❌') }}
                  </span>
                </div>
                <p class="mb-1 text-sm"><strong>تاريخ المعايشة:</strong> {{ offer.trialDate }}</p>
                <p class="mb-1 text-sm"><strong>المكان:</strong> {{ offer.location }}</p>
              </div>
            }
          </div>
        }
      }
    </div>
  `
})
export class ProfileOffersTabComponent {
  @Input() userRole: Role | undefined;
  @Input() myOffers: TrialOffer[] = [];
  @Input() sentOffers: TrialOffer[] = [];
  @Output() statusChange = new EventEmitter<{ offerId: number; status: 'ACCEPTED' | 'REJECTED' }>();
}
