import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { PlayerPosition, PlayerPositionLabels, Role } from '../../../core/models/models';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {
  public selectedRole: Role = 'PLAYER';
  public step = 1;

  // ── Account ──
  public fullName = '';
  public email = '';
  public password = '';
  public confirmPassword = '';
  public acceptTerms = false;
  public guardianName = '';
  public guardianPhone = '';
  public guardianEmail = '';
  public guardianConsent = false;

  // ── PLAYER ──
  public birthDate = '';
  public nationality = '';
  public cityCountry = '';
  public phone = '';
  public position: PlayerPosition = 'STRIKER';
  public secondaryPosition: PlayerPosition | '' = '';
  public preferredFoot: 'RIGHT' | 'LEFT' | 'BOTH' | '' = '';
  public heightCm: number | null = null;
  public weightKg: number | null = null;
  public currentTeam = '';
  public competitionLevel = '';
  public videoUrl = '';
  public bio = '';
  public availableForTransfer = false;      // ← NEW: متاح للانتقال
  public playerGoal = '';                    // ← NEW: هدف التسجيل

  // ── ACADEMY ──
  public organizationName = '';
  public organizationCity = '';
  public organizationPhone = '';
  public organizationWebsite = '';
  public organizationDescription = '';
  public academyAgeGroups: string[] = [];   // ← NEW: الفئات العمرية
  public academyTrainingType = '';           // ← NEW: نوع التدريب

  // ── CLUB ──
  public clubLeagueLevel = '';              // ← NEW: مستوى الدوري
  public clubWebsite = '';                  // ← NEW: موقع النادي
  public clubDescription = '';             // ← NEW: وصف النادي
  public clubActivelyRecruiting = false;   // ← NEW: يبحث عن لاعبين حالياً

  public readonly playerPositions = Object.keys(PlayerPositionLabels) as PlayerPosition[];
  public readonly playerPositionLabels = PlayerPositionLabels;
  public readonly minBirthDate = this.toDateInputValue(new Date(new Date().getFullYear() - 60, 0, 1));
  public readonly maxBirthDate = this.toDateInputValue(new Date());

  public readonly ageGroups = ['U9', 'U11', 'U13', 'U15', 'U17', 'U19', 'سنيور'];

  public isLoading = false;
  public errorMessage = '';
  public successMessage = '';
  public registrationCompleted = false;

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
    if (roleParam === 'PLAYER' || roleParam === 'ACADEMY' || roleParam === 'CLUB') {
      this.selectedRole = roleParam;
    }
  }

  public selectRole(role: Role): void {
    this.selectedRole = role;
    this.step = role === 'PLAYER' ? 1 : 2;
    this.errorMessage = '';
    this.successMessage = '';
    this.registrationCompleted = false;
  }

  public toggleAgeGroup(group: string): void {
    const idx = this.academyAgeGroups.indexOf(group);
    if (idx >= 0) {
      this.academyAgeGroups.splice(idx, 1);
    } else {
      this.academyAgeGroups.push(group);
    }
  }

  public isAgeGroupSelected(group: string): boolean {
    return this.academyAgeGroups.includes(group);
  }

  public isMinor(): boolean {
    if (!this.birthDate) return false;
    const birth = new Date(`${this.birthDate}T00:00:00`);
    const today = new Date();
    let age = today.getFullYear() - birth.getFullYear();
    const beforeBirthday = today.getMonth() < birth.getMonth() || (today.getMonth() === birth.getMonth() && today.getDate() < birth.getDate());
    if (beforeBirthday) age--;
    return age < 18;
  }

  public goToProfileDetails(): void {
    this.errorMessage = '';
    if (
      this.selectedRole === 'PLAYER' &&
      (!this.birthDate || !this.nationality.trim() || !this.cityCountry.trim() || !this.phone.trim() || !this.preferredFoot)
    ) {
      this.errorMessage = 'أكمل تاريخ الميلاد والجنسية ومكان الإقامة ورقم الهاتف والقدم المفضلة للمتابعة.';
      return;
    }
    this.step = 2;
  }

  public goToAccountDetails(): void {
    this.step = 1;
    this.errorMessage = '';
  }

  public onSubmit(): void {
    this.errorMessage = '';

    if (!this.fullName.trim() || !this.email.trim() || !this.password) {
      this.errorMessage = 'يرجى استكمال الاسم والبريد الإلكتروني وكلمة المرور.';
      return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.email.trim())) {
      this.errorMessage = 'يرجى إدخال بريد إلكتروني صحيح.';
      return;
    }
    if (this.password.length < 12 || this.password.length > 72 || new TextEncoder().encode(this.password).length > 72) {
      this.errorMessage = 'كلمة المرور يجب أن تتكون من 12 حرفًا على الأقل وبحد أقصى 72 بايت.';
      return;
    }
    if (this.password !== this.confirmPassword) {
      this.errorMessage = 'كلمتا المرور غير متطابقتين.';
      return;
    }
    if (!this.acceptTerms) {
      this.errorMessage = 'يرجى الموافقة على شروط الاستخدام وسياسة الخصوصية.';
      return;
    }
    if (
      this.selectedRole === 'PLAYER' &&
      (!this.birthDate || !this.nationality.trim() || !this.cityCountry.trim() || !this.phone.trim() || !this.preferredFoot || !this.position)
    ) {
      this.step = 1;
      this.errorMessage = 'يرجى إكمال البيانات الأساسية للاعب أولاً.';
      return;
    }
    if (this.selectedRole === 'PLAYER' && this.isMinor()
      && (!this.guardianName.trim() || !this.guardianPhone.trim() || !this.guardianEmail.trim() || !this.guardianConsent)) {
      this.step = 1;
      this.errorMessage = 'حساب اللاعب دون 18 عامًا يحتاج بيانات ولي الأمر وبريده وموافقته.';
      return;
    }
    if (this.isMinor() && this.guardianEmail.trim().toLowerCase() === this.email.trim().toLowerCase()) {
      this.errorMessage = 'استخدم بريدًا إلكترونيًا مستقلًا لولي الأمر.';
      return;
    }
    if (
      this.selectedRole !== 'PLAYER' &&
      (!this.organizationName.trim() || !this.organizationCity.trim() || !this.organizationPhone.trim())
    ) {
      this.errorMessage = this.selectedRole === 'ACADEMY'
        ? 'أدخل اسم الأكاديمية ومقرها ورقم التواصل للمتابعة.'
        : 'أدخل اسم النادي ومقره ورقم التواصل للمتابعة.';
      return;
    }

    const payload: Record<string, unknown> = {
      email: this.email.trim(),
      password: this.password,
      fullName: this.fullName.trim(),
      role: this.selectedRole,
      termsAccepted: this.acceptTerms
    };

    if (this.selectedRole === 'PLAYER') {
      const profile = {
        birthDate: this.birthDate,
        nationality: this.nationality.trim(),
        cityCountry: this.cityCountry.trim(),
        phone: this.phone.trim(),
        position: this.position,
        secondaryPositions: this.secondaryPosition ? [this.secondaryPosition] : [],
        preferredFoot: this.preferredFoot,
        heightCm: this.heightCm,
        weightKg: this.weightKg,
        currentTeam: this.currentTeam.trim() || null,
        competitionLevel: this.competitionLevel || null,
        videoUrl: this.videoUrl.trim() || null,
        videoUrls: this.videoUrl.trim() ? [this.videoUrl.trim()] : [],
        bio: this.bio.trim() || null,
        availableForTransfer: this.availableForTransfer,
        playerGoal: this.playerGoal || null,
        guardianName: this.isMinor() ? this.guardianName.trim() : null,
        guardianPhone: this.isMinor() ? this.guardianPhone.trim() : null,
        guardianEmail: this.isMinor() ? this.guardianEmail.trim() : null,
        guardianConsent: this.isMinor() ? this.guardianConsent : null
      };
      Object.assign(payload, profile);
      (payload as any).playerProfile = profile;

    } else if (this.selectedRole === 'ACADEMY') {
      Object.assign(payload, {
        academyProfile: {
          academyName: this.organizationName.trim(),
          cityCountry: this.organizationCity.trim(),
          phone: this.organizationPhone.trim(),
          websiteOrPageUrl: this.organizationWebsite.trim() || null,
          description: this.organizationDescription.trim() || null,
          ageGroups: this.academyAgeGroups,
          trainingType: this.academyTrainingType || null
        }
      });

    } else {
      Object.assign(payload, {
        clubProfile: {
          clubName: this.organizationName.trim(),
          cityCountry: this.organizationCity.trim(),
          phone: this.organizationPhone.trim(),
          websiteOrPageUrl: this.clubWebsite.trim() || null,
          description: this.clubDescription.trim() || null,
          leagueLevel: this.clubLeagueLevel || null,
          activelyRecruiting: this.clubActivelyRecruiting
        }
      });
    }

    this.isLoading = true;
    this.authService.register(payload).subscribe({
      next: (res) => {
        this.isLoading = false;
        if (res.success) {
          this.registrationCompleted = true;
          this.successMessage = 'إذا كان البريد قابلًا للتسجيل، ستصلك رسالة تأكيد. للاعبين دون 18 عامًا سيصل طلب موافقة مستقل إلى ولي الأمر. حسابات الأندية والأكاديميات تنتظر المراجعة بعد تأكيد البريد.';
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'حدث خطأ أثناء التسجيل، يرجى مراجعة البيانات والمحاولة مرة أخرى.';
      }
    });
  }

  private toDateInputValue(date: Date): string {
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${date.getFullYear()}-${month}-${day}`;
  }
}
