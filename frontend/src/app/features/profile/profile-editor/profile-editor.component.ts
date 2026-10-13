import { Component, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { PlayerService } from '../../../core/services/player.service';
import { AcademyService } from '../../../core/services/academy.service';
import { ClubService } from '../../../core/services/club.service';
import { TrialOfferService } from '../../../core/services/trial-offer.service';
import { BookmarkService } from '../../../core/services/bookmark.service';
import { PlayerPosition, PlayerPositionLabels, PlayerProfile, TrialOffer } from '../../../core/models/models';
import { RadarChartComponent } from '../../../shared/components/radar-chart/radar-chart.component';

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
    position: 'STRIKER' as PlayerPosition,
    secondaryPositions: [] as PlayerPosition[],
    videoUrl: '',
    videoUrls: [] as string[],
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

  // Academy Model
  public academyForm = {
    academyName: '',
    websiteOrPageUrl: '',
    logoUrl: '',
    description: ''
  };

  // Club Model
  public clubForm = {
    clubName: '',
    logoUrl: '',
    cityCountry: ''
  };

  public playerPositions = Object.keys(PlayerPositionLabels) as PlayerPosition[];
  public playerPositionLabels = PlayerPositionLabels;

  // Trial Offers & Watchlist
  public myOffers: TrialOffer[] = [];
  public sentOffers: TrialOffer[] = [];
  public bookmarkedPlayers: PlayerProfile[] = [];

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
        error: () => {}
      });
    } else if (role === 'ACADEMY' || role === 'CLUB') {
      this.trialOfferService.getSentOffers().subscribe({
        next: (res) => { if (res.success && res.data) this.sentOffers = res.data; },
        error: () => {}
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
      error: () => {}
    });
  }

  public removeBookmark(player: PlayerProfile): void {
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

    if (this.newVideoUrl && this.newVideoUrl.trim().length > 0) {
      this.errorMessage = '';
      this.playerForm.videoUrls.push(this.newVideoUrl.trim());
      if (!this.playerForm.videoUrl) {
        this.playerForm.videoUrl = this.newVideoUrl.trim();
      }
      this.newVideoUrl = '';
    }
  }

  public removeVideoUrl(index: number): void {
    if (this.playerForm.videoUrls && index >= 0 && index < this.playerForm.videoUrls.length) {
      this.playerForm.videoUrls.splice(index, 1);
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
        this.playerService.getPlayerById(user.userId).subscribe({
          next: (res) => {
            this.isLoading = false;
            if (res.success && res.data) {
              const p = res.data;
              this.playerForm = {
                fullName: p.fullName || user.fullName || '',
                age: p.age || 18,
                position: p.position || 'STRIKER',
                secondaryPositions: p.secondaryPositions ? [...p.secondaryPositions] : [],
                videoUrl: p.videoUrl || '',
                videoUrls: p.videoUrls && p.videoUrls.length > 0 ? [...p.videoUrls] : (p.videoUrl ? [p.videoUrl] : []),
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
        this.academyService.getAcademyById(user.userId).subscribe({
          next: (res) => {
            this.isLoading = false;
            if (res.success && res.data) {
              const a = res.data;
              this.academyForm = {
                academyName: a.academyName || user.fullName || '',
                websiteOrPageUrl: a.websiteOrPageUrl || '',
                logoUrl: a.logoUrl || '',
                description: a.description || ''
              };
            }
          },
          error: () => { this.isLoading = false; }
        });
      }
    } else if (role === 'CLUB') {
      const user = this.currentUser();
      if (user) {
        this.clubService.getClubById(user.userId).subscribe({
          next: (res) => {
            this.isLoading = false;
            if (res.success && res.data) {
              const c = res.data;
              this.clubForm = {
                clubName: c.clubName || user.fullName || '',
                logoUrl: c.logoUrl || '',
                cityCountry: c.cityCountry || ''
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
          }
        },
        error: (err) => {
          this.isSaving = false;
          this.errorMessage = err.error?.message || 'فشل حفظ البيانات، يرجى المحاولة مجدداً.';
        }
      });
    } else if (role === 'ACADEMY') {
      this.academyService.updateMyProfile(this.academyForm).subscribe({
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
