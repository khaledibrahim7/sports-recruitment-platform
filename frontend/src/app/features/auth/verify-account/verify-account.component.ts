import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-verify-account',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <main class="auth-container">
      <section class="glass-card auth-card text-center" aria-live="polite">
        <h2>تأكيد الحساب</h2>
        <p>{{ message }}</p>
        @if (canResend) {
          <label for="accountEmail">بريد حساب اللاعب</label>
          <input id="accountEmail" type="email" [(ngModel)]="email" maxlength="150" autocomplete="email" class="form-control mb-3" />
          <button type="button" class="btn btn-outline mb-3" [disabled]="resending || !email" (click)="resend()">
            {{ resending ? 'جارٍ الإرسال...' : 'إعادة إرسال رابط التأكيد' }}
          </button>
        }
        @if (finished) { <a routerLink="/login" class="btn btn-primary">الانتقال لتسجيل الدخول</a> }
      </section>
    </main>
  `,
  styles: [`:host { display:block; padding:3rem 1rem; } .auth-card { max-width:34rem; margin:auto; padding:2rem; }`]
})
export class VerifyAccountComponent implements OnInit {
  message = 'جارٍ التحقق من الرابط...';
  finished = false;
  canResend = false;
  resending = false;
  email = '';

  constructor(private route: ActivatedRoute, private router: Router, private auth: AuthService) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token') ?? '';
    const kind = this.route.snapshot.queryParamMap.get('kind') === 'guardian' ? 'guardian' : 'email';
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { token: null, kind: null },
      queryParamsHandling: 'merge',
      replaceUrl: true
    });
    if (!token) {
      this.message = 'رابط التأكيد غير صالح أو انتهت صلاحيته.';
      this.finished = true;
      this.canResend = true;
      return;
    }
    this.auth.verifyAccount(token, kind).subscribe({
      next: () => {
        this.message = kind === 'guardian'
          ? 'تم تأكيد موافقة ولي الأمر. يمكن تسجيل الدخول بعد تأكيد بريد اللاعب أيضًا.'
          : 'تم تأكيد البريد. حساب المؤسسة ينتظر مراجعة الإدارة إن كان هذا حساب نادي أو أكاديمية.';
        this.finished = true;
      },
      error: () => {
        this.message = 'الرابط غير صالح أو مستخدم من قبل أو انتهت صلاحيته. اطلب رابط تأكيد جديدًا.';
        this.finished = true;
        this.canResend = true;
      }
    });
  }

  resend(): void {
    this.resending = true;
    this.auth.resendVerification(this.email).subscribe({
      next: () => {
        this.message = 'إذا كان الحساب يحتاج تأكيدًا، سنرسل رابطًا جديدًا عند انتهاء مهلة الإرسال.';
        this.resending = false;
      },
      error: () => {
        this.message = 'تعذر إرسال الطلب الآن. حاول مرة أخرى لاحقًا.';
        this.resending = false;
      }
    });
  }
}
