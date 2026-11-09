import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface AcademyFormModel {
  academyName: string;
  websiteOrPageUrl: string;
  cityCountry: string;
  contactPhone: string;
  logoUrl: string;
  description: string;
  ageGroups: string;
  trainingType: string;
}

@Component({
  selector: 'app-profile-academy-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="form-group">
      <label>اسم الأكاديمية *</label>
      <input type="text" [(ngModel)]="form.academyName" name="academyName" required class="form-control" />
    </div>

    <div class="form-group">
      <label>المقر: المدينة والدولة</label>
      <input type="text" [(ngModel)]="form.cityCountry" name="academyCityCountry" class="form-control" />
    </div>

    <div class="form-group">
      <label>رقم هاتف الأكاديمية</label>
      <input type="tel" [(ngModel)]="form.contactPhone" name="academyContactPhone" class="form-control" />
    </div>

    <div class="form-group">
      <label>رابط صفحة الأكاديمية الرسمية *</label>
      <input type="url" [(ngModel)]="form.websiteOrPageUrl" name="websiteOrPageUrl" required class="form-control" />
    </div>

    <div class="form-group">
      <label>رابط اللوجو / الشعار</label>
      <input type="text" [(ngModel)]="form.logoUrl" name="logoUrl" placeholder="رابط خارجي اختياري أو ارفع الشعار بالأسفل" class="form-control" />
      <input type="file" accept="image/jpeg,image/png,image/webp,image/avif" (change)="logoUpload.emit($event)" class="form-control mt-2" aria-label="رفع شعار الأكاديمية" />
    </div>

    <div class="form-group">
      <label>وصف عن الأكاديمية</label>
      <textarea [(ngModel)]="form.description" name="description" class="form-control"></textarea>
    </div>
    <div class="form-group">
      <label>الفئات العمرية</label>
      <input type="text" [(ngModel)]="form.ageGroups" name="academyAgeGroups" class="form-control" placeholder="مثال: U13, U15, U17" />
    </div>
    <div class="form-group">
      <label>نوع التدريب</label>
      <input type="text" [(ngModel)]="form.trainingType" name="academyTrainingType" class="form-control" />
    </div>
  `
})
export class ProfileAcademyFormComponent {
  @Input({ required: true }) form!: AcademyFormModel;
  @Output() logoUpload = new EventEmitter<Event>();
}
