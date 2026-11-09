import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface ClubFormModel {
  clubName: string;
  logoUrl: string;
  cityCountry: string;
  contactPhone: string;
  websiteOrPageUrl: string;
  description: string;
  leagueLevel: string;
  activelyRecruiting: boolean;
}

@Component({
  selector: 'app-profile-club-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="form-group">
      <label>اسم النادي *</label>
      <input type="text" [(ngModel)]="form.clubName" name="clubName" required class="form-control" />
    </div>

    <div class="form-group">
      <label>رابط اللوجو / الشعار الرسمي للنادي *</label>
      <input type="text" [(ngModel)]="form.logoUrl" name="logoUrl" placeholder="رابط خارجي اختياري أو ارفع الشعار بالأسفل" class="form-control" />
      <input type="file" accept="image/jpeg,image/png,image/webp,image/avif" (change)="logoUpload.emit($event)" class="form-control mt-2" aria-label="رفع شعار النادي" />
    </div>

    <div class="form-group">
      <label>المدينة / الدولة</label>
      <input type="text" [(ngModel)]="form.cityCountry" name="cityCountry" class="form-control" />
    </div>

    <div class="form-group">
      <label>رقم هاتف النادي</label>
      <input type="tel" [(ngModel)]="form.contactPhone" name="clubContactPhone" class="form-control" />
    </div>
    <div class="form-group">
      <label>موقع أو صفحة النادي</label>
      <input type="url" [(ngModel)]="form.websiteOrPageUrl" name="clubWebsite" class="form-control" />
    </div>
    <div class="form-group">
      <label>مستوى الدوري</label>
      <input type="text" [(ngModel)]="form.leagueLevel" name="clubLeagueLevel" class="form-control" />
    </div>
    <div class="form-group">
      <label>نبذة عن النادي</label>
      <textarea [(ngModel)]="form.description" name="clubDescription" class="form-control"></textarea>
    </div>
    <label class="form-check-label mb-3">
      <input type="checkbox" [(ngModel)]="form.activelyRecruiting" name="clubActivelyRecruiting" />
      النادي يبحث عن لاعبين حالياً
    </label>
  `
})
export class ProfileClubFormComponent {
  @Input({ required: true }) form!: ClubFormModel;
  @Output() logoUpload = new EventEmitter<Event>();
}
