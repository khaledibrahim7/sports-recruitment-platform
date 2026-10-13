import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { Role } from '../../../core/models/models';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {
  public selectedRole: Role = 'PLAYER';
  public email = '';
  public password = '';
  public confirmPassword = '';

  public isLoading = false;
  public errorMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/profile']);
      return;
    }

    const roleParam = this.route.snapshot.queryParams['role'];
    if (roleParam === 'PLAYER') {
      this.selectedRole = 'PLAYER';
    } else if (roleParam === 'ACADEMY') {
      this.selectedRole = 'ACADEMY';
    } else if (roleParam === 'CLUB') {
      this.selectedRole = 'CLUB';
    }
  }

  public selectRole(role: Role): void {
    this.selectedRole = role;
    this.errorMessage = '';
  }

  public onSubmit(): void {
    if (!this.email || !this.password) {
      this.errorMessage = 'يرجى إدخال البريد الإلكتروني وكلمة المرور';
      return;
    }

    if (this.password !== this.confirmPassword) {
      this.errorMessage = 'كلمتا المرور غير متطابقتين';
      return;
    }

    const payload = {
      email: this.email,
      password: this.password,
      role: this.selectedRole
    };

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.register(payload).subscribe({
      next: (res) => {
        this.isLoading = false;
        if (res.success) {
          // Instantly redirect to dashboard profile editor
          this.router.navigate(['/profile']);
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'حدث خطأ أثناء التسجيل، يرجى مراجعة البريد الإلكتروني وكلمة المرور.';
      }
    });
  }
}
