import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Title } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  private readonly title = inject(Title);
  public email = '';
  public password = '';
  public isLoading = false;
  public errorMessage = '';
  public requiresVerification = false;
  private returnUrl = '/';

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  }

  ngOnInit(): void {
    this.title.setTitle('تسجيل الدخول | الكورة لعبتنا');
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/profile']);
    }
  }

  public onSubmit(): void {
    const normalizedEmail = this.email.trim().toLowerCase();
    if (!normalizedEmail || !this.password) {
      this.errorMessage = 'يرجى كتابة البريد الإلكتروني وكلمة المرور';
      return;
    }
    if (normalizedEmail.length > 150 || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(normalizedEmail)) {
      this.errorMessage = 'يرجى إدخال بريد إلكتروني صحيح.';
      return;
    }
    if (new TextEncoder().encode(this.password).length > 72) {
      this.errorMessage = 'كلمة المرور لا يمكن أن تتجاوز 72 بايت.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.requiresVerification = false;

    this.authService.login({ email: normalizedEmail, password: this.password }).subscribe({
      next: (res) => {
        this.isLoading = false;
        if (res.success) {
          this.router.navigateByUrl(this.returnUrl);
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'بيانات الدخول غير صحيحة، يرجى المحاولة مجدداً.';
        this.requiresVerification = err.status === 403 && err.error?.errorCode === 'ACCOUNT_PENDING';
      }
    });
  }
}
