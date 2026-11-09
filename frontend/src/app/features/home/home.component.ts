import { Component, OnInit, computed, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Meta, Title } from '@angular/platform-browser';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { PlayerService } from '../../core/services/player.service';
import { AcademyService } from '../../core/services/academy.service';
import { ClubService } from '../../core/services/club.service';
import {
  PlayerPositionLabels,
  PublicPlayerProfile,
  AcademyProfile,
  ClubProfile,
  handleImageFallback
} from '../../core/models/models';
import { VideoModalComponent } from '../../shared/components/video-modal/video-modal.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, VideoModalComponent],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly title = inject(Title);
  private readonly meta = inject(Meta);
  public readonly authService = inject(AuthService);
  private readonly playerService = inject(PlayerService);
  private readonly academyService = inject(AcademyService);
  private readonly clubService = inject(ClubService);

  public readonly onAvatarError = handleImageFallback;
  public isLoggedIn = computed(() => this.authService.isLoggedIn());
  public currentUser = computed(() => this.authService.currentUserSignal());

  public userRole = computed(() => this.currentUser()?.role);
  public userDisplayName = computed(() => {
    const user = this.currentUser();
    if (!user) return 'المستخدم';
    return user.email ? user.email.split('@')[0] : 'المستخدم';
  });

  public featuredPlayers: PublicPlayerProfile[] = [];
  public registeredPlayerCount = 0;
  public isLoadingFeatured = false;
  public featuredLoadError = '';
  public featuredAcademies: AcademyProfile[] = [];
  public featuredClubs: ClubProfile[] = [];
  public playerPositionLabels = PlayerPositionLabels;

  public get spotlightPlayer(): PublicPlayerProfile | null {
    return this.featuredPlayers && this.featuredPlayers.length > 0 ? this.featuredPlayers[0] : null;
  }

  public mediaUrl(value?: string | null): string { return this.playerService.imageUrl(value); }

  public isVideoModalOpen = false;
  public selectedVideoUrl = '';
  public selectedVideoUrls: string[] = [];
  public selectedPlayerName = '';

  ngOnInit(): void {
    this.title.setTitle('الكورة لعبتنا | اكتشاف المواهب الرياضية');
    this.meta.updateTag({ name: 'description', content: 'منصة تربط لاعبي كرة القدم بالأكاديميات والأندية، مع ملفات فنية وبدنية وفرص معايشة.' });
    this.meta.updateTag({ property: 'og:title', content: 'الكورة لعبتنا | اكتشاف المواهب الرياضية' });
    this.meta.updateTag({ property: 'og:description', content: 'اكتشف المواهب وتصفح الملفات الفنية للاعبين.' });
    if (isPlatformBrowser(this.platformId)) this.loadFeaturedData();
  }

  public openVideo(player: PublicPlayerProfile): void {
    if (player.videoUrls && player.videoUrls.length > 0) {
      this.selectedVideoUrls = player.videoUrls;
      this.selectedVideoUrl = '';
    } else {
      this.selectedVideoUrls = [];
      this.selectedVideoUrl = player.videoUrl || '';
    }
    this.selectedPlayerName = player.fullName;
    this.isVideoModalOpen = true;
  }

  public closeVideoModal(): void {
    this.isVideoModalOpen = false;
  }

  public reloadFeaturedData(): void {
    this.featuredPlayers = [];
    this.registeredPlayerCount = 0;
    this.featuredAcademies = [];
    this.featuredClubs = [];
    this.loadFeaturedData();
  }

  private loadFeaturedData(): void {
    this.isLoadingFeatured = true;
    this.featuredLoadError = '';
    let completed = 0;
    const finish = (failed: boolean) => {
      if (failed) this.featuredLoadError = 'تعذر تحميل بعض بيانات المنصة. تحقق من الاتصال ثم أعد المحاولة.';
      completed += 1;
      if (completed === 3) this.isLoadingFeatured = false;
    };
    this.playerService.getPlayers('', null, '', 0, 6).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.featuredPlayers = res.data.items;
          this.registeredPlayerCount = res.data.totalElements;
          finish(false);
        } else finish(true);
      },
      error: () => finish(true)
    });

    this.academyService.getAcademies().subscribe({
      next: (res) => { if (res.success && res.data) { this.featuredAcademies = res.data; finish(false); } else finish(true); },
      error: () => finish(true)
    });

    this.clubService.getClubs().subscribe({
      next: (res) => { if (res.success && res.data) { this.featuredClubs = res.data; finish(false); } else finish(true); },
      error: () => finish(true)
    });
  }
}
