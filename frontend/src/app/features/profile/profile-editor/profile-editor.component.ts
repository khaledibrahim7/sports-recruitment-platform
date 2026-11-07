import { Component, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { PlayerService } from '../../../core/services/player.service';
import { AcademyService } from '../../../core/services/academy.service';
import { ClubService } from '../../../core/services/club.service';
import { TrialOfferService } from '../../../core/services/trial-offer.service';
import { BookmarkService } from '../../../core/services/bookmark.service';
import { PlayerPosition, PlayerPositionLabels, PlayerProfile, PublicPlayerProfile, TrialOffer } from '../../../core/models/models';
import { RadarChartComponent } from '../../../shared/components/radar-chart/radar-chart.component';
import { parseSupportedVideoUrl } from '../../../shared/components/video-modal/video-modal.component';

@Component({
  selector: 'app-profile-editor',
  standalone: true,
  imports: [CommonModule, FormsModule, RadarChartComponent],
  templateUrl: './profile-editor.component.html',
  styleUrls: ['./profile-editor.component.scss']
})
export class ProfileEditorComponent implements OnInit {
  public currentUser = computed(() => this.authService.currentUserSignal());
  public userRole = computed(() => this.currentUser()?.role);

  public activeTab: 'EDIT' | 'OFFERS' | 'WATCHLIST' = 'EDIT';

  // Player Model
  public playerForm = {
    fullName: '',
    age: 18,
    birthDate: '',
    nationality: '',
    cityCountry: '',
    currentTeam: '',
    competitionLevel: '',
    playerGoal: '',
    availableForTransfer: false,
    position: 'STRIKER' as PlayerPosition,
    secondaryPositions: [] as PlayerPosition[],
    videoUrl: '',
    videoUrls: [] as string[],
    uploadedVideoUrls: [] as string[],
    removedUploadedVideoUrls: [] as string[],
    phone: '',
    bio: '',
    avatarUrl: '',
    heightCm: 175,
    weightKg: 70,
    preferredFoot: 'RIGHT' as 'RIGHT' | 'LEFT' | 'BOTH',
    pace: 75,
    shooting: 70,
    passing: 70,
    dribbling: 75,
    defending: 65,
    physicality: 70
  };

  public toggleSecondaryPosition(pos: PlayerPosition): void {
    if (pos === this.playerForm.position) return;
    const idx = this.playerForm.secondaryPositions.indexOf(pos);
    if (idx >= 0) {
      this.playerForm.secondaryPositions.splice(idx, 1);
    } else {
      this.playerForm.secondaryPositions.push(pos);
    }
  }

  public isSecondaryPositionSelected(pos: PlayerPosition): boolean {
    return this.playerForm.secondaryPositions.includes(pos);
  }

  public newVideoUrl = '';
  public isUploadingVideo = false;
  public isUploadingImage = false;
  public mediaUrl(value?: string | null): string { return this.playerService.imageUrl(value); }

  public uploadAvatarFile(event: Event): void {
    this.uploadProfileImage(event, url => { this.playerForm.avatarUrl = url; });
  }

  public uploadAcademyLogo(event: Event): void {
    this.uploadProfileImage(event, url => { this.academyForm.logoUrl = url; });
  }

  public uploadClubLogo(event: Event): void {
    this.uploadProfileImage(event, url => { this.clubForm.logoUrl = url; });
  }

  private uploadProfileImage(event: Event, setUrl: (url: string) => void): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    const allowedTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/avif'];
    if (!allowedTypes.includes(file.type.toLowerCase()) || file.size <= 0 || file.size > 10 * 1024 * 1024) {
      this.errorMessage = 'الصيغ المدعومة JPG وPNG وWebP وAVIF، وبحجم أقصى 10 ميجابايت.';
      input.value = '';
      return;
    }
    this.isUploadingImage = true;
    this.errorMessage = '';
    this.playerService.createImageUploadUrl(file).subscribe({
      next: (targetResponse) => {
        const target = targetResponse.data;
        if (!targetResponse.success || !target || file.size > target.maxBytes) {
          this.isUploadingImage = false;
          this.errorMessage = 'تعذر تجهيز رفع الصورة أو تجاوزت الحجم المسموح.';
          input.value = '';
          return;
        }
        this.playerService.uploadFileToStorage(target.uploadUrl, file, target.contentType).subscribe({
          next: () => this.playerService.confirmImageUpload(target.key).subscribe({
            next: (confirmation) => {
              this.isUploadingImage = false;
              if (confirmation.success && confirmation.data) {
                setUrl(confirmation.data);
                this.successMessage = 'تم رفع الصورة إلى مساحة المنصة.';
              }
              input.value = '';
            },
            error: () => {
              this.isUploadingImage = false;
              this.errorMessage = 'تم رفع الصورة لكن تعذر تأكيدها.';
              input.value = '';
            }
          }),
          error: () => {
            this.isUploadingImage = false;
            this.errorMessage = 'تعذر رفع الصورة إلى التخزين. تحقق من إعداد CORS للحاوية.';
            input.value = '';
          }
        });
      },
      error: () => {
        this.isUploadingImage = false;
        this.errorMessage = 'تعذر تجهيز رابط رفع الصورة.';
        input.value = '';
      }
    });
  }

  public uploadVideoFile(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    if (file.size > 100 * 1024 * 1024) {
      this.errorMessage = 'الحد الأقصى لحجم الفيديو 100 ميجابايت.';
      input.value = '';
      return;
    }
    const extension = file.name.split('.').pop()?.toLowerCase();
    if (!['mp4', 'webm', 'mov', 'm4v', 'ogg'].includes(extension || '') || (file.type && !file.type.toLowerCase().startsWith('video/'))) {
      this.errorMessage = 'الصيغ المدعومة: MP4 وWebM وMOV وM4V وOGG.';
      input.value = '';
      return;
    }
    if (this.playerForm.videoUrls.length >= this.MAX_VIDEOS) {
      this.errorMessage = 'وصلت للحد الأقصى وهو 3 فيديوهات.';
      input.value = '';
      return;
    }
    this.isUploadingVideo = true;
    this.errorMessage = '';
    this.playerService.createVideoUploadUrl(file).subscribe({
      next: (targetResponse) => {
        if (!targetResponse.success || !targetResponse.data) {
          this.isUploadingVideo = false;
          this.errorMessage = 'تعذر تجهيز مساحة رفع الفيديو.';
          input.value = '';
          return;
        }
        const target = targetResponse.data;
        if (file.size <= 0 || file.size > target.maxBytes) {
          this.isUploadingVideo = false;
          this.errorMessage = 'حجم الفيديو يتجاوز الحد المسموح به (100 ميجابايت).';
          input.value = '';
          return;
        }
        this.playerService.uploadFileToStorage(target.uploadUrl, file, target.contentType).subscribe({
          next: () => this.playerService.confirmVideoUpload(target.key).subscribe({
            next: (res) => {
              this.isUploadingVideo = false;
              if (res.success && res.data) {
                this.playerForm.videoUrls.push(res.data);
                this.playerForm.uploadedVideoUrls.push(res.data);
                this.playerForm.videoUrl = this.playerForm.videoUrls[0];
                this.successMessage = 'تم رفع الفيديو إلى التخزين السحابي.';
              }
              input.value = '';
            },
            error: () => {
              this.isUploadingVideo = false;
              this.errorMessage = 'تم رفع الملف لكن تعذر تأكيده. أعد المحاولة بعد تحديث الصفحة.';
              input.value = '';
            }
          }),
          error: (err) => {
            this.isUploadingVideo = false;
            this.errorMessage = err.error?.message || 'تعذر إرسال الملف إلى التخزين. تحقق من إعداد CORS للحاوية.';
            input.value = '';
          }
        });
      },
      error: (err) => {
        this.isUploadingVideo = false;
        this.errorMessage = err.error?.message || 'تعذر تجهيز رفع الفيديو. تحقق من إعداد التخزين السحابي.';
        input.value = '';
      }
    });
  }

  // Academy Model
  public academyForm = {
    academyName: '',
    websiteOrPageUrl: '',
    cityCountry: '',
    contactPhone: '',
    logoUrl: '',
    description: '',
    ageGroups: '' as string,
    trainingType: ''
  };

  // Club Model
  public clubForm = {
    clubName: '',
    logoUrl: '',
    cityCountry: '',
    contactPhone: '',
    websiteOrPageUrl: '',
    description: '',
    leagueLevel: '',
    activelyRecruiting: false
  };

  public playerPositions = Object.keys(PlayerPositionLabels) as PlayerPosition[];
  public playerPositionLabels = PlayerPositionLabels;

  // Trial Offers & Watchlist
  public myOffers: TrialOffer[] = [];
  public sentOffers: TrialOffer[] = [];
  public bookmarkedPlayers: PublicPlayerProfile[] = [];

  public isLoading = false;
  public isSaving = false;
  public successMessage = '';
  public errorMessage = '';

  constructor(
    public authService: AuthService,
    private playerService: PlayerService,
    private academyService: AcademyService,
    private clubService: ClubService,
    private trialOfferService: TrialOfferService,
    private bookmarkService: BookmarkService
  ) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  public readonly MAX_VIDEOS = 3;

  public setTab(tab: 'EDIT' | 'OFFERS' | 'WATCHLIST'): void {
    this.activeTab = tab;
    if (tab === 'OFFERS') {
      this.loadOffers();
    } else if (tab === 'WATCHLIST') {
      this.loadWatchlist();
    }
  }

  public loadOffers(): void {
    const role = this.userRole();
    if (role === 'PLAYER') {
      this.trialOfferService.getMyOffers().subscribe({
        next: (res) => { if (res.success && res.data) this.myOffers = res.data; },
        error: () => { this.errorMessage = 'تعذر تحميل طلبات المعايشة. تحقق من الاتصال وحاول مرة أخرى.'; }
      });
    } else if (role === 'ACADEMY' || role === 'CLUB') {
      this.trialOfferService.getSentOffers().subscribe({
        next: (res) => { if (res.success && res.data) this.sentOffers = res.data; },
        error: () => { this.errorMessage = 'تعذر تحميل الطلبات المرسلة. تحقق من الاتصال وحاول مرة أخرى.'; }
      });
    }
  }

  public updateOfferStatus(offerId: number, status: 'ACCEPTED' | 'REJECTED'): void {
    this.trialOfferService.updateStatus(offerId, status).subscribe({
      next: (res) => {
        if (res.success) {
          this.successMessage = res.message;
          this.loadOffers();
        }
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'فشل تغيير حالة الطلب.';
      }
    });
  }

  public loadWatchlist(): void {
    this.bookmarkService.getBookmarkedPlayers().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.bookmarkedPlayers = res.data;
        }
      },
      error: () => { this.errorMessage = 'تعذر تحميل قائمة المتابعة. تحقق من الاتصال وحاول مرة أخرى.'; }
    });
  }

  public removeBookmark(player: PublicPlayerProfile): void {
    this.bookmarkService.toggleBookmark(player.id).subscribe({
      next: () => {
        this.loadWatchlist();
      }
    });
  }

  public addVideoUrl(): void {
    if (!this.playerForm.videoUrls) {
      this.playerForm.videoUrls = [];
    }

    if (this.playerForm.videoUrls.length >= this.MAX_VIDEOS) {
      this.errorMessage = 'عفواً، الحد الأقصى المسموح به هو 3 فيديوهات كحد أقصى لكل لاعب.';
      return;
    }

    const trimmed = this.newVideoUrl?.trim() || '';
    if (!trimmed) return;

    // ── منع تكرار نفس الرابط ──
    const normalized = (value: string) => {
      const parsed = parseSupportedVideoUrl(value.trim());
      if (parsed?.type === 'youtube') return `youtube:${parsed.youtubeId}`;
      try {
        const url = new URL(value.trim());
        if (parsed?.type === 'drive') return `drive:${url.pathname.match(/^\/file\/d\/([\w-]+)/)?.[1]}`;
        if (parsed?.type === 'vimeo') return `vimeo:${url.pathname.match(/^\/(?:video\/)?(\d+)/)?.[1]}`;
        return `${url.hostname.toLowerCase()}${url.pathname.replace(/\/$/, '')}${url.search}`;
      } catch { return value.trim().toLowerCase(); }
    };
    if (this.playerForm.videoUrls.some(url => normalized(url) === normalized(trimmed))) {
      this.errorMessage = 'هذا الفيديو مضاف بالفعل — حتى لو اختلف شكل رابط YouTube.';
      return;
    }

    // Use the same strict HTTPS/provider allowlist as the player.
    const parsed = parseSupportedVideoUrl(trimmed);
    if (!parsed || parsed.type === 'hosted') {
      this.errorMessage = 'يُدعم: YouTube — Google Drive — Vimeo — ملف MP4 مباشر';
      return;
    }

    this.errorMessage = '';
    this.playerForm.videoUrls.push(trimmed);
    if (!this.playerForm.videoUrl) {
      this.playerForm.videoUrl = trimmed;
    }
    this.newVideoUrl = '';
  }

  public removeVideoUrl(index: number): void {
    if (this.playerForm.videoUrls && index >= 0 && index < this.playerForm.videoUrls.length) {
      const removedUrl = this.playerForm.videoUrls[index];
      this.playerForm.videoUrls.splice(index, 1);
      this.playerForm.uploadedVideoUrls = this.playerForm.uploadedVideoUrls.filter(url => url !== removedUrl);
      if (removedUrl.startsWith('platform-video:')) {
        this.playerForm.removedUploadedVideoUrls.push(removedUrl.substring('platform-video:'.length));
      }
      if (this.playerForm.videoUrls.length > 0) {
        this.playerForm.videoUrl = this.playerForm.videoUrls[0];
      } else {
        this.playerForm.videoUrl = '';
      }
    }
  }

  public loadProfile(): void {
    const role = this.userRole();
    if (!role) return;

    this.isLoading = true;

    if (role === 'PLAYER') {
      const user = this.currentUser();
      if (user) {
        this.playerService.getMyProfile().subscribe({
          next: (res) => {
            this.isLoading = false;
            if (res.success && res.data) {
              const p = res.data;
              this.playerForm = {
                fullName: p.fullName || user.fullName || '',
                age: p.age || 18,
                birthDate: p.birthDate || '',
                nationality: p.nationality || '',
                cityCountry: p.cityCountry || '',
                currentTeam: p.currentTeam || '',
                competitionLevel: p.competitionLevel || '',
                playerGoal: p.playerGoal || '',
                availableForTransfer: p.availableForTransfer || false,
                position: p.position || 'STRIKER',
                secondaryPositions: p.secondaryPositions ? [...p.secondaryPositions] : [],
                videoUrl: p.videoUrl || '',
                videoUrls: p.videoUrls && p.videoUrls.length > 0 ? [...p.videoUrls] : (p.videoUrl ? [p.videoUrl] : []),
                uploadedVideoUrls: p.uploadedVideoUrls ? [...p.uploadedVideoUrls] : [],
                removedUploadedVideoUrls: [],
                phone: p.phone || '',
                bio: p.bio || '',
                avatarUrl: p.avatarUrl || '',
                heightCm: p.heightCm || 175,
                weightKg: p.weightKg || 70,
                preferredFoot: p.preferredFoot || 'RIGHT',
                pace: p.pace || 75,
                shooting: p.shooting || 70,
                passing: p.passing || 70,
                dribbling: p.dribbling || 75,
                defending: p.defending || 65,
                physicality: p.physicality || 70
              };
            }
          },
          error: () => { this.isLoading = false; }
        });
      }
    } else if (role === 'ACADEMY') {
      const user = this.currentUser();
      if (user) {
        this.academyService.getMyProfile().subscribe({
          next: (res) => {
            this.isLoading = false;
            if (res.success && res.data) {
              const a = res.data;
              this.academyForm = {
                academyName: a.academyName || user.fullName || '',
                websiteOrPageUrl: a.websiteOrPageUrl || '',
                cityCountry: a.cityCountry || '',
                contactPhone: a.contactPhone || '',
                logoUrl: a.logoUrl || '',
                description: a.description || '',
                ageGroups: (a.ageGroups || []).join(', '),
                trainingType: a.trainingType || ''
              };
            }
          },
          error: () => { this.isLoading = false; }
        });
      }
    } else if (role === 'CLUB') {
      const user = this.currentUser();
      if (user) {
        this.clubService.getMyProfile().subscribe({
          next: (res) => {
            this.isLoading = false;
            if (res.success && res.data) {
              const c = res.data;
              this.clubForm = {
                clubName: c.clubName || user.fullName || '',
                logoUrl: c.logoUrl || '',
                cityCountry: c.cityCountry || '',
                contactPhone: c.contactPhone || '',
                websiteOrPageUrl: c.websiteOrPageUrl || '',
                description: c.description || '',
                leagueLevel: c.leagueLevel || '',
                activelyRecruiting: c.activelyRecruiting || false
              };
            }
          },
          error: () => { this.isLoading = false; }
        });
      }
    }
  }

  public saveProfile(): void {
    const role = this.userRole();
    if (!role) return;

    this.isSaving = true;
    this.successMessage = '';
    this.errorMessage = '';

    if (role === 'PLAYER') {
      this.playerService.updateMyProfile(this.playerForm).subscribe({
        next: (res) => {
          this.isSaving = false;
          if (res.success) {
            this.successMessage = 'تم حفظ بروفايل اللاعب ومؤشرات المهارة بنجاح!';
            this.authService.updateProfileInSession(res.data);
            if (this.playerForm.removedUploadedVideoUrls.length > 0) {
              this.playerService.deleteRemovedVideos(this.playerForm.removedUploadedVideoUrls).subscribe({
                error: () => { this.errorMessage = 'تم حفظ البروفايل، لكن تعذر حذف بعض الفيديوهات القديمة من التخزين.'; }
              });
              this.playerForm.removedUploadedVideoUrls = [];
            }
          }
        },
        error: (err) => {
          this.isSaving = false;
          this.errorMessage = err.error?.message || 'فشل حفظ البيانات، يرجى المحاولة مجدداً.';
        }
      });
    } else if (role === 'ACADEMY') {
      this.academyService.updateMyProfile({ ...this.academyForm, ageGroups: this.academyForm.ageGroups.split(',').map(value => value.trim()).filter(Boolean) }).subscribe({
        next: (res) => {
          this.isSaving = false;
          if (res.success) {
            this.successMessage = 'تم حفظ بيانات الأكاديمية بنجاح!';
            this.authService.updateProfileInSession(res.data);
          }
        },
        error: (err) => {
          this.isSaving = false;
          this.errorMessage = err.error?.message || 'فشل حفظ البيانات، يرجى المحاولة مجدداً.';
        }
      });
    } else if (role === 'CLUB') {
      this.clubService.updateMyProfile(this.clubForm).subscribe({
        next: (res) => {
          this.isSaving = false;
          if (res.success) {
            this.successMessage = 'تم حفظ بيانات النادي بنجاح!';
            this.authService.updateProfileInSession(res.data);
          }
        },
        error: (err) => {
          this.isSaving = false;
          this.errorMessage = err.error?.message || 'فشل حفظ البيانات، يرجى المحاولة مجدداً.';
        }
      });
    }
  }
}
